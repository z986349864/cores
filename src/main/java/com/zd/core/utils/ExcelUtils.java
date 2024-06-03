package com.zd.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zd.core.excel.constrant.ExcelConstants;
import com.zd.core.exception.BusinessException;
import com.zd.core.mq.constant.MessageConstants;
import org.apache.commons.collections.MapUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

public class ExcelUtils {

    static Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * @param multipartFile 输入流
     * @param startRowNum   开始读取的行(0开始) 不能小于0
     * Description: excel文件转换为一个map.  map的key为行数，value 中的 key 为列数，value中的value 为值
     */
    @Deprecated
    public static Map<Integer, Map<Integer, String>> readToMapByFile(MultipartFile multipartFile, int startRowNum) {
        return readToMapByFile(multipartFile, 0, startRowNum, (long) ExcelConstants.MAX_IMPORT_UPLOAD);
    }

    public static Map<Integer, Map<Integer, String>> readToMapByFile(MultipartFile multipartFile,
                                                                     int sheetIdx,
                                                                     int startRowNum,
                                                                     Long maxSize) {
        return readToMapByFile(multipartFile, sheetIdx, startRowNum, maxSize, false);
    }

    public static Map<Integer, Map<Integer, String>> readToMapByFile(MultipartFile multipartFile,
                                                                     int sheetIdx,
                                                                     int startRowNum,
                                                                     Long maxSize,
                                                                     boolean rawValue) {
        if (multipartFile.getSize() > maxSize) {
            throw new BusinessException(MessageConstants.Business.EXCEL_TOO_LARGE);
        }
        if (!multipartFile.getOriginalFilename().toLowerCase().endsWith(ExcelConstants.FILE_EXTENSION_XLSX)) {
            throw new BusinessException(MessageConstants.Business.EXCEL_FORMAT_ERROR);
        }
        InputStream inputStream = null;
        //后面的###代表保留后面10位小数，会自动进行调整如果是1000000000.25632则会显示1000000000.25632
        DecimalFormat df = new DecimalFormat("0.##########");
        try {
            inputStream = multipartFile.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(sheetIdx);
            Map<Integer, Map<Integer, String>> resultData = new HashMap<Integer, Map<Integer, String>>();
            for (int i = startRowNum; i < sheet.getLastRowNum() + 1; i++) {
                XSSFRow xssfrow = sheet.getRow(i);
                Map<Integer, String> cellData = new HashMap<Integer, String>();
                String cellValue = "";
                //处理空行
                if (xssfrow == null) {
                    if (i == startRowNum) {
                        return resultData;
                    }
                    resultData.put(i + 1, null);
                    continue;
                }
                for (int k = 0; k < xssfrow.getLastCellNum(); k++) {
                    //处理空列
                    XSSFCell cell = xssfrow.getCell(k);
                    if (cell == null) {
                        cellData.put(k + 1, "");
                        continue;
                    }
                    if (rawValue) {
                        cellValue = cell.getRawValue();
                        if (cell.getCellType() == cell.CELL_TYPE_STRING) {
                            cellValue = cell.getStringCellValue();
                        }
                        cellData.put(k + 1, cellValue);
                        continue;
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                                //  如果是date类型则 ，获取该cell的date值
                                cellValue = DateUtils.convert(cell.getDateCellValue());
                            } else { // 纯数字
                                cellValue = df.format(cell.getNumericCellValue());
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            cellValue = cell.getStringCellValue();
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            cellValue = cell.getBooleanCellValue() + "";
                            break;
                        default:
                            cellValue = "";
                            break;
                    }
                    cellData.put(k + 1, cellValue);
                }
                resultData.put(i + 1, cellData);
            }
            return resultData;
        } catch (Exception e) {
            logger.error("excel文件解析失败!!!", e);
            throw new BusinessException(MessageConstants.Business.EXCEL_TEMPLATE_INVALID);
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error("输入流关闭失败", e);
            }
        }
    }

    public static byte[] additionalErrorColumn2File(MultipartFile multipartFile,
                                                    Map<Integer,Map<Integer, CellValue>> rowErrorCellMap) {
        if (multipartFile.getSize() > ExcelConstants.MAX_IMPORT_UPLOAD) {
            throw new BusinessException(MessageConstants.Business.EXCEL_TOO_LARGE);
        }
        if (!multipartFile.getOriginalFilename().toLowerCase().endsWith(ExcelConstants.FILE_EXTENSION_XLSX)) {
            throw new BusinessException(MessageConstants.Business.EXCEL_FORMAT_ERROR);
        }
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();

            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            if(MapUtils.isNotEmpty(rowErrorCellMap)){
                rowErrorCellMap.forEach((sheetIdx,errorMap) ->{
                    if(Objects.isNull(sheetIdx) || MapUtils.isEmpty(errorMap)){
                        return;
                    }
                    XSSFSheet sheet = workbook.getSheetAt(sheetIdx);
                    int lastRowNo = sheet.getLastRowNum() + 1;
                    XSSFRow headerRow = sheet.getRow(0);
                    short lastCellNum = headerRow.getLastCellNum();
                    int exitErrorCol = -1;
                    for (int col = 0; col < lastCellNum; col++) {
                        XSSFCell cell = headerRow.getCell(col);
                        if (Objects.equals(cell.getStringCellValue(), errorMap.values().iterator().next().getStringValue())) {
                            headerRow.removeCell(cell);
                            exitErrorCol = col;
                            break;
                        }
                    }
                    if (-1 != exitErrorCol) {
                        lastCellNum--;
                    }
                    /**
                     * do add cells in row and rows select
                     */
                    for (int j = 0; j < lastRowNo; j++) {

                        XSSFRow row = sheet.getRow(j);
                        if (Objects.isNull(row)) {
                            break;
                        }
                        if (-1 != exitErrorCol) {
                            XSSFCell errorCell = row.getCell(exitErrorCol);
                            if (Objects.nonNull(errorCell)) {
                                row.removeCell(errorCell);
                            }
                        }
                        CellValue cellValue = errorMap.get(j + 1);
                        if (Objects.isNull(cellValue)) {
                            continue;
                        }
                        XSSFCell xssfCell = row.createCell(lastCellNum, Cell.CELL_TYPE_ERROR);
                        xssfCell.setCellValue(cellValue.getStringValue());
                    }
                    Set<Integer> selectRows = errorMap.keySet();
                    for (int i = 0; i < lastRowNo; i++) {

                        XSSFRow xssfrow = sheet.getRow(i);
                        if (!selectRows.contains(i + 1)) {
                            sheet.removeRow(xssfrow);
                        }
                    }
                });
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("excel文件解析失败!!!", e);
            throw new BusinessException(MessageConstants.Business.EXCEL_TEMPLATE_INVALID);
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error("输入流关闭失败", e);
            }
        }
    }


    /**
     * @param excelFile   输入流
     * @param startRowNum 开始读取的行(0开始) 不能小于0
     * @param maxSize     限定一个最大文件SIZE
     * Description: excel文件转换为一个map.  map的key为行数，value 中的 key 为列数，value中的value 为值
     */
    public static Map<Integer, Map<Integer, String>> readToMapByFileObj(Object[] excelFile,
                                                                        int startRowNum,
                                                                        Long maxSize) {
        return readToMapByFileObj(excelFile, 0, startRowNum, maxSize);
    }


    /**
     * @param excelFile   输入流
     * @param startRowNum 开始读取的行(0开始) 不能小于0
     * @param maxSize     限定一个最大文件SIZE
     * Description: excel文件转换为一个map.  map的key为行数，value 中的 key 为列数，value中的value 为值
     */
    public static Map<Integer, Map<Integer, String>> readToMapByFileObj(Object[] excelFile,
                                                                        int sheetIdx,
                                                                        int startRowNum,
                                                                        Long maxSize) {
        if (excelFile == null || excelFile.length <= 0 || excelFile[0] == null) {
            throw new BusinessException(MessageConstants.Business.EXCEL_DATA_EMPTY);
        }
        JSONObject jsonObj = JSON.parseObject(excelFile[0].toString());
        String fileName = jsonObj.getString("name");
        Long fileSize = jsonObj.getLong("size");
        String str = jsonObj.getString("url");
        String base64Data = !StringUtils.isEmpty(str) && str.lastIndexOf("base64,") != -1
                ? str.substring(str.lastIndexOf("base64,") + "base64,".length())
                : str;
        if (fileSize > maxSize) {
            throw new BusinessException(MessageConstants.Business.EXCEL_TOO_LARGE);
        }
        if (!fileName.endsWith(ExcelConstants.FILE_EXTENSION_XLSX)) {
            throw new BusinessException(MessageConstants.Business.EXCEL_FORMAT_ERROR);
        }
        InputStream inputStream = null;
        try {
            byte[] data = Base64.getDecoder().decode(base64Data.getBytes());
            inputStream = new ByteArrayInputStream(data);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(sheetIdx);
            Map<Integer, Map<Integer, String>> resultData = new HashMap<Integer, Map<Integer, String>>();
            for (int i = startRowNum; i < sheet.getLastRowNum() + 1; i++) {
                XSSFRow XSSFRow = sheet.getRow(i);
                Map<Integer, String> cellData = new HashMap<Integer, String>();
                String cellValue = "";
                //处理空行
                if (XSSFRow == null) {
                    if (i == startRowNum) {
                        return resultData;
                    }
                    resultData.put(i + 1, null);
                    continue;
                }
                for (int k = 0; k < XSSFRow.getLastCellNum(); k++) {
                    //处理空列
                    if (XSSFRow.getCell(k) == null) {
                        cellData.put(k + 1, "");
                        continue;
                    }
                    switch (XSSFRow.getCell(k).getCellType()) {
                        case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(XSSFRow.getCell(k))) {
                                //  如果是date类型则 ，获取该cell的date值
                                cellValue = DateUtils.convert(XSSFRow.getCell(k).getDateCellValue());
                                break;
                            } else { // 纯数字
                                XSSFRow.getCell(k).setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING);
                            }
                        case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
                            cellValue = XSSFRow.getCell(k).getStringCellValue();
                            break;
                        case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN:
                            cellValue = XSSFRow.getCell(k).getBooleanCellValue() + "";
                            break;
                        default:
                            cellValue = "";
                            break;
                    }
                    cellData.put(k + 1, cellValue);
                }
                resultData.put(i + 1, cellData);
            }
            return resultData;
        } catch (Exception e) {
            logger.error("excel文件解析失败!!!", e);
            throw new BusinessException(MessageConstants.Business.EXCEL_TEMPLATE_INVALID);
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error("输入流关闭失败", e);
            }
        }
    }


    /**
     * 使用jdbc游标方式导出excel
     *
     * @param jdbcTemplate 注入执行脚本的jdbcTemplate
     * @param fileName     文件名
     * @param titleNames   表头
     * @param sql          需要执行的sql
     * @param params       sql参数，没有传递null
     * @param response
     */
    public static void exportDataWithJdbc(JdbcTemplate jdbcTemplate,
                                          String fileName,
                                          List<String> titleNames,
                                          String sql,
                                          List<Object> params,
                                          HttpServletResponse response) {
        OutputStream outputStream = null;
        if (StringUtils.isEmpty(fileName)) {
            throw new BusinessException("文件名不合法!!!");
        }
        fileName += ExcelConstants.FILE_EXTENSION_XLSX;
        try {
            fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
            // 声明一个工作薄
            SXSSFWorkbook workbook = new SXSSFWorkbook(200);
            CreationHelper createHelper = workbook.getCreationHelper();
            List<Sheet> sheets = new ArrayList<Sheet>(3);
            Sheet sheet = workbook.createSheet();
            sheets.add(sheet);
            // 设置表格默认列宽度为20个字节
            sheet.setDefaultColumnWidth(20);
            // 产生表格标题行
            Row row = sheet.createRow(0);
            for (int i = 0; i < titleNames.size(); i++) {
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
                cell.setCellValue(createHelper.createRichTextString(titleNames.get(i)));
            }
            SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, params.toArray());
            int index = 1;
            int sheetIndex = 0;
            while (rs.next()) {
                if (index == SpreadsheetVersion.EXCEL2007.getLastRowIndex() + 1) {
                    index = 0;
                    sheetIndex++;
                    sheets.add(workbook.createSheet());
                }
                row = sheets.get(sheetIndex).createRow(index++);
                for (int k = 0; k < titleNames.size(); k++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(k);
                    //默认按列头顺序读取
                    cell.setCellValue(rs.getString(k + 1));
                }
            }
            // 清空response
            response.reset();
            setResponse(response, fileName);
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据构建好的数据对象生成excel文件并用流直接写到客户端
     *
     * @param maps              表头
     * @param listData          表格数据
     * @param fileName          文件名称
     * @param dateFormatPattern 导出中的时间格式
     * @param response          请求响应对象
     * @return
     */
    public static <T> void excelExport(Map<String, String> maps,
                                       List<T> listData,
                                       String fileName,
                                       String dateFormatPattern,
                                       HttpServletResponse response) {
        if (StringUtils.isEmpty(fileName)) {
            throw new BusinessException("文件名不合法!!!");
        }
        fileName += ExcelConstants.FILE_EXTENSION_XLSX;
        Workbook wb = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        OutputStream toClient = null;
        try {
            fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
            wb = new XSSFWorkbook();
            CreationHelper createHelper = wb.getCreationHelper();
            Sheet sheet = wb.createSheet("sheet1");
            Set<String> sets = maps.keySet();
            Row row = sheet.createRow(0);
            int i = 0;
            // 定义表头
            for (String key : sets) {
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(i++);
                cell.setCellValue(createHelper.createRichTextString(maps
                        .get(key)));
            }
            int size = listData.size();
            for (int j = 0; j < size; j++) {
                T p = listData.get(j);
                Class classType = p.getClass();
                int index = 0;
                Row row1 = sheet.createRow(j + 1);
                for (String key : sets) {
                    String firstLetter = key.substring(0, 1).toUpperCase();
                    // 获得和属性对应的getXXX()方法的名字
                    String getMethodName = "get" + firstLetter
                            + key.substring(1);
                    // 获得和属性对应的getXXX()方法
                    Method getMethod = classType.getMethod(getMethodName);
                    // 调用原对象的getXXX()方法
                    Object value = getMethod.invoke(p);
                    if (value instanceof Date) {
                        if (StringUtils.isEmpty(dateFormatPattern)) {
                            value = DateUtils.convert((Date) value);
                        } else {
                            value = DateUtils.convert((Date) value, dateFormatPattern);
                        }
                    }
                    org.apache.poi.ss.usermodel.Cell cell = row1.createCell(index++);
                    cell.setCellValue(value == null ? "" : value.toString());
                }
            }
            byteArrayOutputStream = new ByteArrayOutputStream();
            wb.write(byteArrayOutputStream);
            byteArrayOutputStream.flush();
            byte[] aa = byteArrayOutputStream.toByteArray();
            // 清空response
            response.reset();
            setResponse(response, fileName);
            toClient = new BufferedOutputStream(response.getOutputStream());
            toClient.write(aa);
            toClient.flush();
        } catch (IOException e) {
            logger.error("IOException", e);
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException", e);
        } catch (NoSuchMethodException e) {
            logger.error("NoSuchMethodException", e);
        } catch (InvocationTargetException e) {
            logger.error("InvocationTargetException", e);
        } catch (Exception e) {
            logger.error("Exception", e);
        } finally {
            try {
                if (null != byteArrayOutputStream) {
                    byteArrayOutputStream.close();
                }
                if (null != toClient) {
                    toClient.close();
                }
            } catch (IOException e) {
                logger.error("输入流关闭失败", e);
            }
        }
    }

    /**
     * @param maps           <String,String> maps
     * @param listData       <T> list 需要导出的数据列表对象
     * @param file           excel文件 支持 xlsx 和 xls 文件
     * @param dateFormatType 0位不带时分秒 1为带时分秒  没有时间的导出忽略
     * @return
     */
    public static <T> void writeData(Map<String, String> maps, List<T> listData, File file, Integer
            dateFormatType, Integer startRow) {
        if (file == null) {
            throw new BusinessException("文件不能为空!!!");
        }
        FileOutputStream out = null;
        try {
            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet();
            writeHeader(maps, file, sheet);
            Set<String> sets = maps.keySet();
            if (startRow == null) {
                startRow = 1;
            }
            int size = listData.size();
            for (int j = 0; j < size; j++) {
                Row row = sheet.createRow(j + startRow);
                T p = listData.get(j);
                setRowData(sets, dateFormatType, row, p);
            }
            out = new FileOutputStream(file);
            wb.write(out);
            out.flush();
        } catch (Exception e) {
            logger.error("IOException", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                logger.error("输入流关闭失败", e);
            }
        }
    }


    /**
     * @param maps           <String,String> maps
     * @param listData       <T> list 需要导出的数据列表对象
     * @param file           excel文件 支持 xlsx 和 xls 文件
     * @param dateFormatType 0位不带时分秒 1为带时分秒  没有时间的导出忽略
     * @return
     */
    public static <T> void writeData
    (Map<String, Map<String, String>> maps, Map<String, List<T>> listData, File file, Integer
            dateFormatType, Integer startRow) {
        if (file == null) {
            throw new BusinessException("文件不能为空!!!");
        }
        FileOutputStream out = null;
        try {
            Workbook wb = new XSSFWorkbook();
            for (String key : maps.keySet()) {
                Sheet sheet = wb.createSheet(key);
                writeHeader(maps.get(key), file, sheet);
                Set<String> sets = maps.get(key).keySet();
                if (startRow == null) {
                    startRow = 1;
                }
                int size = listData.get(key).size();
                for (int j = 0; j < size; j++) {
                    Row row = sheet.createRow(j + startRow);
                    T p = listData.get(key).get(j);
                    setRowData(sets, dateFormatType, row, p);
                }
            }

            out = new FileOutputStream(file);
            wb.write(out);
            out.flush();
        } catch (Exception e) {
            logger.error("IOException", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                logger.error("输入流关闭失败", e);
            }
        }
    }


    /**
     * 按行写数据到excel中
     *
     * @param sets
     * @param dateFormatType
     * @param row
     * @param p
     * @param <T>
     */
    private static <T> void setRowData(Set<String> sets, Integer dateFormatType, Row row, T p) {
        int index = 0;
        Class classType = p.getClass();
        for (Iterator<String> it = sets.iterator(); it.hasNext(); ) {
            try {
                String key = it.next();
                String firstLetter = key.substring(0, 1).toUpperCase();
                // 获得和属性对应的getXXX()方法的名字
                String getMethodName = "get" + firstLetter
                        + key.substring(1);
                // 获得和属性对应的getXXX()方法
                Method getMethod = classType.getMethod(getMethodName,
                        new Class[]{});
                // 调用原对象的getXXX()方法
                Object value = getMethod.invoke(p, new Object[]{});
                if (value instanceof Date) {
                    if (dateFormatType == ExcelConstants.DATE_FORMAT_TYPE_ALL) {
                        value = DateUtils.convert((Date) value);
                    } else {
                        value = DateUtils.convert((Date) value, DateUtils.DATE_FORMAT);
                    }
                }
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(index++);
                cell.setCellValue(value == null ? "" : value.toString());
            } catch (Exception e) {
                logger.error("write excel error", e);
            }
        }
    }

    /**
     * 写excel头部
     *
     * @param maps <String,String> maps
     * @param file excel文件
     * @return
     */
    private static void writeHeader(Map<String, String> maps, File file, Sheet sheet) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            Workbook wb = new XSSFWorkbook();
            CreationHelper createHelper = wb.getCreationHelper();
            Set<String> sets = maps.keySet();
            Row row = sheet.createRow(0);
            int i = 0;
            // 定义表头
            for (Iterator<String> it = sets.iterator(); it.hasNext(); ) {
                String key = it.next();
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(i++);
                cell.setCellValue(createHelper.createRichTextString(maps.get(key)));
            }
            wb.write(os);
            os.flush();
        } catch (Exception e) {
            logger.error("IOException", e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                logger.error("输入流关闭失败", e);
            }
        }
    }

    /**
     * 设置返回头信息
     *
     * @param response
     * @param fileName
     */
    private static void setResponse(HttpServletResponse response, String fileName) {
        // 设置response的Header
        response.addHeader("Content-Disposition", "attachment;filename="
                + new String(fileName.getBytes()));
        response.setContentType("application/vnd.ms-excel;charset=ISO8859-1");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }
}
