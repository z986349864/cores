package com.zd.core.excel.common;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.zd.core.config.SftpProperties;
import com.zd.core.excel.annotation.*;
import com.zd.core.excel.entity.CellError;
import com.zd.core.excel.entity.CellValue;
import com.zd.core.excel.entity.RowBase;
import com.zd.core.exception.BusinessException;
import com.zd.core.i18n.LocaleMessageSource;
import com.zd.core.mq.constant.MessageConstants;
import com.zd.core.mq.failretry.service.IDistributedIdGenerateService;
import com.zd.core.utils.ExcelUtils;
import com.zd.core.utils.ListUtils;
import com.zd.core.utils.ReflectUtils;
import com.zd.core.utils.tool.FtpClient;
import com.zd.core.utils.tool.SftpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.zd.core.excel.common.ErrorTypeEnum.INVALID_VALUE;
import static com.zd.core.excel.common.ErrorTypeEnum.LENGTH_ERROR;

/**
 * @Title: ExcelAnalyse
 * @Description: Excel工具类，检查并返回数据
 */
@Component
@Slf4j
public class CoreExcelAnalyse implements IExcelAnalyse{

    @Autowired
    private LocaleMessageSource localeMessageSource;

    @Autowired
    private SftpProperties sftpProperties;

    @Autowired
    private IDistributedIdGenerateService idGenerateService;

    /**
     * excel解析单个sheet页,指定sheet页的位置和起始行
     * @param multipartFile
     * @param sheetIdx
     * @param startRowNum
     * @param maxSize
     * @param targetClass
     * @param rowValue
     * @param applicationContext
     * @param <T>
     * @return
     */
    @Override
    public <T extends RowBase> List<T> analyseSingle(MultipartFile multipartFile, int sheetIdx, int startRowNum, long maxSize, @NotNull Class<T> targetClass, boolean rowValue, ApplicationContext applicationContext) {
        Map<Integer, Map<Integer, String>> excelData = ExcelUtils.readToMapByFile(multipartFile, sheetIdx, startRowNum, maxSize, rowValue);
        List<T> list = null;
        try {
            list = excelData2List(excelData, targetClass, applicationContext);
        } catch (Exception e) {
            log.error("error: ", e);
            throw e;
        }
        return list;
    }

    /**
     * excel解析单个sheet页,固定第一个sheet页，从第一行开始
     * @param multipartFile
     * @param maxSize
     * @param targetClass
     * @param applicationContext
     * @param <T>
     * @return
     */
    @Override
    public <T extends RowBase> List<T> analyseSingle(MultipartFile multipartFile, long maxSize, @NotNull Class<T> targetClass,  ApplicationContext applicationContext) {
        Map<Integer, Map<Integer, String>> excelData = ExcelUtils.readToMapByFile(multipartFile, 0, 0, maxSize, false);
        List<T> list = null;
        try {
            list = excelData2List(excelData, targetClass, applicationContext);
        } catch (Exception e) {
            log.error("error: ", e);
            throw e;
        }
        return list;
    }

    /**
     * @param excelData
     * @param targetClass
     * @param <T>
     * @return
     */
    public static <T extends RowBase> List<T> excelData2List(Map<Integer, Map<Integer, String>> excelData, @NotNull Class<T> targetClass, @NotNull ApplicationContext applicationContext) {
        List<T> beanList = new ArrayList<>();
        if (MapUtils.isEmpty(excelData)) {
            return beanList;
        }
        ExcelData excelAnnotation = targetClass.getAnnotation(ExcelData.class);
        if (Objects.isNull(excelAnnotation)) {
            log.error("lack for ExcelData annotation!");
            return beanList;
        }
        Map<Integer, String> headerLineMap = excelData.remove(1);
        List<Field> fieldList = ReflectUtils.getAllFieldsThenSetAccessible(targetClass);
        /**
         * key is the columnNo, value is bean field
         */
        Map<Integer, Field> columnFieldMap = new HashMap<>();

        List<String> languageList = Lists.newArrayList(excelAnnotation.languages());
        LocaleMessageSource localeMessageSource = applicationContext.getBean(LocaleMessageSource.class);

        Optional.ofNullable(fieldList).orElse(Collections.emptyList()).forEach(field -> {

            Header headerAnnotation = field.getAnnotation(Header.class);
            if (Objects.isNull(headerAnnotation) || StringUtils.isBlank(headerAnnotation.name()) || headerAnnotation.columnNo() == -1) {
                return;
            }
            String headerName = headerLineMap.get(headerAnnotation.columnNo());
            if (StringUtils.isBlank(headerName)) {
                throw new BusinessException(MessageConstants.Business.EXCEL_ANALYSE_FAILED);
            }
            if (excelAnnotation.i18nHeader()) {
                new ArrayList<>(Optional.ofNullable(languageList).orElse(Collections.emptyList())).forEach(language -> {
                    String languageName = localeMessageSource.getMessage(headerAnnotation.name(), Locale.forLanguageTag(language));
                    if (!Objects.equals(headerName, languageName)) {
                        languageList.remove(language);
                    }
                });
                if (CollectionUtils.isEmpty(languageList)) {
                    throw new BusinessException(MessageConstants.Business.EXCEL_ANALYSE_FAILED);
                }
            }
            if (!excelAnnotation.i18nHeader()) {

                if (!Objects.equals(headerName, headerAnnotation.name())) {
                    throw new BusinessException(MessageConstants.Business.EXCEL_ANALYSE_FAILED);
                }
            }
            columnFieldMap.put(headerAnnotation.columnNo(), field);
        });
        /**
         * do field check
         */
        Map<CellValue, List<T>> needCheckUniqueMap = new HashMap<>();

        excelData.entrySet().forEach(rowEntry -> {
            if(rowEntry == null || rowEntry.getValue().values().stream().allMatch(StringUtils::isBlank)){
                log.warn("解析到空白行，已自动忽略.");
                return;
            }
            Integer lineNumber = rowEntry.getKey();
            Map<Integer, String> columnMap = rowEntry.getValue();

            T bean = ReflectUtils.getInstance(targetClass);

            columnMap.entrySet().forEach(columnEntry -> {

                Integer columnNo = columnEntry.getKey();
                String value = columnEntry.getValue();
                Field field = columnFieldMap.get(columnNo);
                if (Objects.isNull(field)) {
                    return;
                }
                ReflectUtils.setFieldVal(bean, field.getName(), value);
            });
            // ignore empty bean(row)
            if (ReflectUtils.isEmpty(bean)) {
                return;
            }
            bean.setAnalyseLanguage(languageList.get(0));
            // set lineNumber
            bean.setLineNumber(lineNumber);
            // initialize errors
            bean.setErrors(new ArrayList<>());

            columnFieldMap.entrySet().forEach(columnEntry -> {

                Integer columnNo = columnEntry.getKey();
                Field field = columnEntry.getValue();
                String value = (String) columnMap.get(columnNo);
                if (Objects.isNull(field)) {
                    return;
                }
                Header headerAnnotation = field.getAnnotation(Header.class);
                if (Objects.isNull(headerAnnotation)) {
                    return;
                }
                String headerName = headerAnnotation.name();
                if (excelAnnotation.i18nHeader()) {
                    headerName = localeMessageSource.getMessage(headerName, Locale.forLanguageTag(languageList.get(0)));
                }
                for (Annotation annotation : Optional.ofNullable(field.getAnnotations()).orElse(new Annotation[0])) {

                    if (Objects.equals(annotation.annotationType(), Required.class)) {

                        if (StringUtils.isBlank(value)) {

                            CellError error = new CellError();
                            error.setColumnNo(columnNo);
                            error.setColumnName(headerName);
                            error.setValue(value);
                            error.setErrorType(ErrorTypeEnum.EMPTY.code);

                            bean.getErrors().add(error);
                        }
                        continue;
                    }
                    if (Objects.equals(annotation.annotationType(), Length.class)) {

                        if (StringUtils.isBlank(value)) {
                            continue;
                        }
                        Length lengthAnnotation = (Length) annotation;
                        String wrapperValue = Optional.ofNullable(value).orElse("");
                        if (wrapperValue.length() < lengthAnnotation.min() || wrapperValue.length() > lengthAnnotation.max()) {

                            CellError error = new CellError();
                            error.setColumnNo(columnNo);
                            error.setColumnName(headerName);
                            error.setValue(value);
                            error.setErrorType(LENGTH_ERROR.code);

                            bean.getErrors().add(error);
                        }
                        continue;
                    }
                    if (Objects.equals(annotation.annotationType(), Range.class)) {

                        if (StringUtils.isBlank(value)) {
                            continue;
                        }
                        Range rangeAnnotation = (Range) annotation;
                        Long longValue = null;
                        try {
                            longValue = Long.parseLong(value);
                        } catch (NumberFormatException e) {
                        }
                        if (Objects.isNull(longValue) || longValue < rangeAnnotation.min() || longValue > rangeAnnotation.max()) {

                            CellError error = new CellError();
                            error.setColumnNo(columnNo);
                            error.setColumnName(headerName);
                            error.setValue(value);
                            error.setErrorType(ErrorTypeEnum.RANGE_ERROR.code);

                            bean.getErrors().add(error);
                        }
                        continue;
                    }
                    if (Objects.equals(annotation.annotationType(), In.class)) {

                        if (StringUtils.isBlank(value)) {
                            continue;
                        }
                        In inAnnotation = (In) annotation;
                        // An initialized value is set to avoid null in the enumeration value
                        String initMappingValue = "-#@init@#-";
                        String mappingValue = initMappingValue;
                        if (inAnnotation.i18nValue()) {

                            String inStr = localeMessageSource.getMessage(inAnnotation.i18nCode(), Locale.forLanguageTag(languageList.get(0)));

                            List<String> inList = JSON.parseArray(inStr, String.class);
                            for (int i = 0; i < inList.size(); i++) {

                                String idxValue = inList.get(i);
                                if (Objects.equals(idxValue, value)) {

                                    mappingValue = inAnnotation.i18nCode() + "[" + i + "]";
                                    // If it is internationalized enumeration value, and a match is made, then the internationalized index matched is set to it.
                                    ReflectUtils.setFieldVal(bean, field.getName(), mappingValue);
                                    break;
                                }
                            }
                        }
                        if (!inAnnotation.i18nValue() && Arrays.stream(inAnnotation.value()).anyMatch(referenceValue -> Objects.equals(referenceValue, value))) {

                            mappingValue = value;
                        }
                        if (Objects.equals(initMappingValue, mappingValue)) {

                            CellError error = new CellError();
                            error.setColumnNo(columnNo);
                            error.setColumnName(headerName);
                            error.setValue(value);
                            error.setErrorType(INVALID_VALUE.code);

                            bean.getErrors().add(error);
                        }
                        continue;
                    }
                    if (Objects.equals(annotation.annotationType(), DataType.class)) {

                        if (StringUtils.isBlank(value)) {
                            continue;
                        }
                        DataType dataTypeAnnotation = (DataType) annotation;
                        try {
                            switch (dataTypeAnnotation.type()) {
                                case LONG:
                                    Long.parseLong(value);
                                    break;
                                case INTEGER:
                                    Integer.parseInt(value);
                                    break;
                                case BIG_DECIMAL:
                                    new BigDecimal(value);
                                    break;
                                default:
                                    break;
                            }
                        } catch (NumberFormatException e) {

                            CellError error = new CellError();
                            error.setColumnNo(columnNo);
                            error.setColumnName(headerName);
                            error.setValue(value);
                            error.setErrorType(INVALID_VALUE.code);

                            bean.getErrors().add(error);
                        }
                        continue;
                    }
                    if (Objects.equals(annotation.annotationType(), Decimal.class)) {

                        if (StringUtils.isBlank(value)) {
                            continue;
                        }
                        Decimal decimalAnnotation = (Decimal) annotation;
                        BigDecimal bigDecimalValue = null;
                        try {
                            bigDecimalValue = new BigDecimal(value);
                        } catch (Exception e) {
                        }
                        if (Objects.isNull(bigDecimalValue) || bigDecimalValue.precision() > decimalAnnotation.precision() || bigDecimalValue.scale() > decimalAnnotation.scale()) {

                            CellError error = new CellError();
                            error.setColumnNo(columnNo);
                            error.setColumnName(headerName);
                            error.setValue(value);
                            error.setErrorType(INVALID_VALUE.code);

                            bean.getErrors().add(error);
                        }
                        continue;
                    }
                    if (Objects.equals(annotation.annotationType(), RequiredBy.class)) {

                        RequiredBy requiredByAnnotation = (RequiredBy) annotation;
                        String fieldValIn = ReflectUtils.getFieldValIgnoreException(bean, requiredByAnnotation.field());
                        List<String> fieldsValList = Arrays.stream(requiredByAnnotation.fields())
                                .map(fieldName ->
                                        (String) ReflectUtils.getFieldValIgnoreException(bean, fieldName))
                                .collect(Collectors.toList());

                        boolean required = false;
                        switch (requiredByAnnotation.type()) {

                            case EXIST_ONE:
                                required = fieldsValList.stream().anyMatch(StringUtils::isNotBlank);
                                break;
                            case ALL_EXIST:
                                required = fieldsValList.stream().allMatch(StringUtils::isNotBlank);
                                break;
                            case VALUE_IN:
                                required = StringUtils.isBlank(fieldValIn) || Arrays.asList(requiredByAnnotation.values()).contains(fieldValIn);
                                break;
                            default:
                                break;
                        }
                        if (required && StringUtils.isBlank(value)) {

                            CellError error = new CellError();
                            error.setColumnNo(columnNo);
                            error.setColumnName(headerName);
                            error.setValue(value);
                            error.setErrorType(ErrorTypeEnum.EMPTY.code);

                            bean.getErrors().add(error);
                        }
                        continue;
                    }
                    if (Objects.equals(annotation.annotationType(), RegexPattern.class)) {

                        if (StringUtils.isBlank(value)) {
                            continue;
                        }
                        RegexPattern patternAnnotation = (RegexPattern) annotation;
                        Pattern pattern = Pattern.compile(patternAnnotation.value(),patternAnnotation.mode());
                        if (!pattern.matcher(value).matches()) {

                            CellError error = new CellError();
                            error.setColumnNo(columnNo);
                            error.setColumnName(headerName);
                            error.setValue(value);
                            error.setErrorType(INVALID_VALUE.code);

                            bean.getErrors().add(error);
                        }
                        continue;
                    }
                    if (Objects.equals(annotation.annotationType(), Exist.class)) {

                        if (StringUtils.isBlank(value)) {
                            continue;
                        }
                        Exist existAnnotation = (Exist) annotation;
                        boolean exist = false;
                        try {
                            Class<?> referenceClass = Class.forName(existAnnotation.classReference());
                            Method existMethod = ReflectUtils.getMethod(referenceClass, existAnnotation.existMethod(), String.class, Object.class);
                            // get bean
                            Object referenceBean = applicationContext.getBean(referenceClass);
                            exist = (Boolean) existMethod.invoke(referenceBean, existAnnotation.fieldName(), value);
                        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                        if (!exist) {

                            CellError error = new CellError();
                            error.setColumnNo(columnNo);
                            error.setColumnName(headerName);
                            error.setValue(value);
                            error.setErrorType(INVALID_VALUE.code);

                            bean.getErrors().add(error);
                        }
                        continue;
                    }
                    if (Objects.equals(annotation.annotationType(), Unique.class)) {

                        if (StringUtils.isBlank(value)) {
                            continue;
                        }
                        Unique uniqueAnnotation = (Unique) annotation;
                        String classReferenceName = uniqueAnnotation.classReference();
                        String existMethodName = uniqueAnnotation.existMethod();
                        if (StringUtils.isNotBlank(classReferenceName) && StringUtils.isNotBlank(existMethodName)) {

                            boolean exist = false;
                            try {
                                Class<?> referenceClass = Class.forName(classReferenceName);
                                Method existMethod = ReflectUtils.getMethod(referenceClass, existMethodName, String.class, Object.class);
                                // get bean
                                Object referenceBean = applicationContext.getBean(referenceClass);
                                exist = (Boolean) existMethod.invoke(referenceBean, uniqueAnnotation.fieldName(), value);
                            } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                            if (exist) {

                                CellError error = new CellError();
                                error.setColumnNo(columnNo);
                                error.setColumnName(headerName);
                                error.setValue(value);
                                error.setErrorType(ErrorTypeEnum.DUPLICATE_IN_DATABASE.code);

                                bean.getErrors().add(error);
                            }
                        }
                        if (!uniqueAnnotation.ignoreInFile()) {

                            CellValue cell = new CellValue();
                            cell.setColumnNo(columnNo);
                            cell.setColumnName(headerName);
                            cell.setValue(value);

                            needCheckUniqueMap.putIfAbsent(cell, new ArrayList<>());
                            needCheckUniqueMap.get(cell).add(bean);
                        }
                    }
                }
            });
            beanList.add(bean);
        });
        if (MapUtils.isEmpty(needCheckUniqueMap)) {
            return beanList;
        }
        needCheckUniqueMap.entrySet().forEach(needCheckUniqueEntry -> {

            CellValue cell = needCheckUniqueEntry.getKey();
            List<T> beans = needCheckUniqueEntry.getValue();

            if (CollectionUtils.isEmpty(beans) || beans.size() == 1) {
                return;
            }
            beans.forEach(bean -> {

                CellError error = new CellError();
                error.setColumnNo(cell.getColumnNo());
                error.setColumnName(cell.getColumnName());
                error.setValue(cell.getValue());
                error.setErrorType(ErrorTypeEnum.DUPLICATE_IN_FILE.code);

                bean.getErrors().add(error);
            });
        });
        return beanList;
    }


    /**
     * 构建解析时的异常信息
     * @param errors
     * @param language
     * @return
     */
    public Set<String> composeErrorMessages(List<CellError> errors, String language) {
        errors = Optional.ofNullable(errors).orElse(Lists.newArrayList()).stream().distinct().collect(Collectors.toList());
        Map<Integer, List<CellError>> errorMapList = ListUtils.list2MapList(errors, "errorType");
        return errorMapList.entrySet().stream().distinct().map(errorEntry -> {

            Integer errorType = errorEntry.getKey();
            List<CellError> errorList = errorEntry.getValue();
            List<String> columnNames = ListUtils.fieldList(errorList, "columnName");
            Object[] i18nArgs = new Object[]{String.join(", ", columnNames)};

            String requiredI18nMessage = localeMessageSource.getMessage(MessageConstants.Business.EXCEL_FIELD_REQUIRED, i18nArgs, Locale.forLanguageTag(language));
            String invalidI18nMessage = localeMessageSource.getMessage(MessageConstants.UserMessageConstants.USER_ENROLLMENT_ERROR_NOT_EXISTS, i18nArgs, Locale.forLanguageTag(language));
            String repeatDatabaseI18nMessage = localeMessageSource.getMessage(MessageConstants.Business.EXCEL_FIELD_REPEAT_DATABASE, i18nArgs, Locale.forLanguageTag(language));
            String repeatFileI18nMessage = localeMessageSource.getMessage(MessageConstants.Business.EXCEL_FIELD_REPEAT_FILE, i18nArgs, Locale.forLanguageTag(language));
            String lengthErrorI18nMessage = localeMessageSource.getMessage(MessageConstants.Business.EXCEL_FIELD_LENGTH_ERROR, i18nArgs, Locale.forLanguageTag(language));
            String rangeErrorI18nMessage = localeMessageSource.getMessage(MessageConstants.Business.EXCEL_FIELD_RANGE_ERROR, i18nArgs, Locale.forLanguageTag(language));

            if (ErrorTypeEnum.EMPTY.check(errorType)) {
                return requiredI18nMessage;
            }
            if (INVALID_VALUE.check(errorType)) {
                return invalidI18nMessage;
            }
            if (ErrorTypeEnum.DUPLICATE_IN_DATABASE.check(errorType)) {
                return repeatDatabaseI18nMessage;
            }
            if (ErrorTypeEnum.DUPLICATE_IN_FILE.check(errorType)) {
                return repeatFileI18nMessage;
            }
            if (LENGTH_ERROR.check(errorType)) {
                return lengthErrorI18nMessage;
            }
            if (ErrorTypeEnum.RANGE_ERROR.check(errorType)) {
                return rangeErrorI18nMessage;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * 保存一个错误文件
     *
     * @return java.lang.String
     */
    public <T> String saveFailedExcel(List<List> failedExcelData) {

        String url = null;

//        SftpClient sftpClient = new SftpClient(sftpProperties.getHost(), sftpProperties.getPort(), sftpProperties.getUsername(), sftpProperties.getPassword());
        FtpClient sftpClient = new FtpClient(sftpProperties.getHost(), sftpProperties.getPort(), sftpProperties.getUsername(), sftpProperties.getPassword());

        try {
            sftpClient.connect();
            String templateName = localeMessageSource.getMessage(MessageConstants.Business.USER_BATCH_UPLOAD_ERROR_EXPORT_FILE_NAME);
            InputStream templateIs = sftpClient.downloadForStream(sftpProperties.getTemplateDir(), templateName);
            String failedExcelName = idGenerateService.generate() + ".xlsx";
            String[] i18nSheetName = new String[]{"test"};

            ByteArrayOutputStream failedExcelOs = new ByteArrayOutputStream();
            ExcelWriter excelWriter = EasyExcel.write(failedExcelOs)
                    .needHead(false)
                    .withTemplate(templateIs)
                    .excelType(ExcelTypeEnum.XLSX)
                    .useDefaultStyle(true)
                    .automaticMergeHead(true)
                    .build();

            for (int i = 0; i < failedExcelData.size(); i++) {
                List sheetData = failedExcelData.get(i);
                if(CollectionUtils.isEmpty(sheetData)){
                    continue;
                }
                Class<?> aClass = sheetData.get(0).getClass();
                List<Field> allFieldsThenSetAccessible = ReflectUtils.getAllFieldsThenSetAccessible(aClass);
                //todo 注意字段定义属性要和excel表头顺序一致，这里暂未实现排序
                for (Field field : allFieldsThenSetAccessible) {

                    ExcelProperty excelPropertyAnnotation = field.getAnnotation(ExcelProperty.class);
                    if (Objects.isNull(excelPropertyAnnotation)) {
                        continue;
                    }
                    Header excelHeaderAnnotation = field.getAnnotation(Header.class);
                    if(Objects.isNull(excelHeaderAnnotation) || StringUtils.isBlank(excelHeaderAnnotation.name())){
                        log.warn("There is no header annotation for the current field[{}]",field.getName());
                        continue;
                    }
                    String i18nHeader = localeMessageSource.getMessage(excelHeaderAnnotation.name() );
                    InvocationHandler invocationHandler = Proxy.getInvocationHandler(excelPropertyAnnotation);
                    Field memberValuesField = ReflectUtils.getFieldThenSetAccessible(invocationHandler.getClass(), "memberValues");
                    Map<String, Object> memberValues = null;
                    try {
                        memberValues = (Map<String, Object>) memberValuesField.get(invocationHandler);
                    } catch (IllegalAccessException e) {
                        log.error("there is some exception in covering the head when write the failed excel.", e);
                        throw new BusinessException(MessageConstants.Business.SYSTEM_ERROR);
                    }
                    memberValues.put("value", new String[]{i18nHeader});
                }
                WriteSheet writeSheet = EasyExcelFactory
                        .writerSheet(i,i18nSheetName[i])
                        .head(aClass)
                        .build();
                excelWriter.write(sheetData, writeSheet);

                excelWriter.finish();
            }
            //上传文档
            ByteArrayInputStream failedExcelIs = new ByteArrayInputStream(failedExcelOs.toByteArray());
            sftpClient.uploadByInputStream(sftpProperties.getImportErrorDir(), failedExcelIs, failedExcelName);

            url = sftpProperties.getPrefixUrl() + File.separatorChar + sftpProperties.getImportErrorDir() + File.separatorChar + failedExcelName;
        } catch (Exception e) {
            log.error("there is some exception write the failed excel.", e);
            throw new BusinessException(MessageConstants.Business.SYSTEM_ERROR);
        } finally {
            sftpClient.close();
        }
        return url;
    }

    /**
     * 保存一个错误文件
     *
     * @return java.lang.String
     */
    public <T> String saveFailedExcel(List<List> data, String language) {

        String url = null;

        SftpClient sftpClient = new SftpClient(sftpProperties.getHost(), sftpProperties.getPort(), sftpProperties.getUsername(), sftpProperties.getPassword());
        try {
            sftpClient.connect();
            String templateName = localeMessageSource.getMessage(MessageConstants.UserMessageConstants.USER_BATCH_UPLOAD_ERROR_EXPORT_FILE_NAME, Locale.forLanguageTag(language));
            InputStream templateIs = sftpClient.downloadForStream(sftpProperties.getTemplateDir(), templateName);
            String failedExcelName = idGenerateService.generate() + ".xlsx";

            ByteArrayOutputStream failedExcelOs = new ByteArrayOutputStream();
            ExcelWriter excelWriter = EasyExcel.write(failedExcelOs)
                    .needHead(false)
                    .withTemplate(templateIs)
                    .excelType(ExcelTypeEnum.XLSX)
                    .useDefaultStyle(true)
                    .automaticMergeHead(true)
                    .build();
            for (int i = 0; i < data.size(); i++) {
                WriteSheet sheet1 = EasyExcel.writerSheet(i).build();
                excelWriter.write(data.get(i), sheet1);
            }
            excelWriter.finish();
            //上传文档
            ByteArrayInputStream failedExcelIs = new ByteArrayInputStream(failedExcelOs.toByteArray());
            sftpClient.uploadByInputStream(sftpProperties.getImportErrorDir(), failedExcelIs, failedExcelName);

            url = sftpProperties.getPrefixUrl() + File.separatorChar + sftpProperties.getImportErrorDir() + File.separatorChar + failedExcelName;
        } catch (Exception e) {
            log.error("there is some exception write the failed excel.", e);
            throw new BusinessException(MessageConstants.Business.SYSTEM_ERROR);
        } finally {
            sftpClient.close();
        }
        return url;
    }
}