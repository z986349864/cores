package com.zd.core.excel.service.impl;

import com.zd.core.excel.common.CoreExcelAnalyse;
import com.zd.core.excel.constrant.ExcelConstants;
import com.zd.core.excel.constrant.UserConstants;
import com.zd.core.excel.service.UserService;
import com.zd.core.excel.vo.CoreImportResultVO;
import com.zd.core.exception.BusinessException;
import com.zd.core.model.UserImportDTO;
import com.zd.core.model.UserVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CoreExcelAnalyse excelAnalyse;

    @Override
    public CoreImportResultVO batchUploadUserList(MultipartFile file) {
        CoreImportResultVO result = new CoreImportResultVO();

        //excel数据解析为
        List<UserImportDTO> importUserList = excelAnalyse.analyseSingle(file, ExcelConstants.MAX_IMPORT_UPLOAD, UserImportDTO.class, applicationContext);
        if (importUserList.size() > UserConstants.USER_BATCH_UPLOAD.MAXIMUM) {
            throw new BusinessException(UserConstants.BATCH_UPLOAD_TOO_MANY, (Object) UserConstants.USER_BATCH_UPLOAD.MAXIMUM);
        }
        //统计导入总数
        result.setTotalCount(importUserList.size());
        //记录成功数量
        AtomicInteger successCountAto = new AtomicInteger();
        //记录异常信息
        Map<Integer, String> errorMap = new HashMap<>();

        //上传处理
        importUserList.forEach(dto ->{
            String id = dto.getId();
            String name = dto.getName();
            String analyseLanguage = dto.getAnalyseLanguage();
            //TODO 失败分2种，一种是严重的错误，不用读取里面的数据。一种是读取导入失败的数据
            //TODO 下面的是严重错误，失败后直接返回
            if(StringUtils.isEmpty(id)){
                String message = "ID IS NULL";
                errorMap.put(dto.getLineNumber(),message);
                return;
            }


            List<String> errorList = Lists.newArrayList();
            //name
            //TODO 读取导入失败的数据
            if(!StringUtils.isEmpty(name)){
                String message = "NAME IS NULL";
                errorList.add(message);
            }

            //excel数据解析时的异常
            Set<String> errorMessageList = excelAnalyse.composeErrorMessages(dto.getErrors(), analyseLanguage);
            if (CollectionUtils.isNotEmpty(errorMessageList)) {
                errorList.add(String.join("; ", errorMessageList));
            }
            errorList.removeIf(StringUtils::isBlank);
            if(CollectionUtils.isNotEmpty(errorList)){
                errorMap.put(dto.getLineNumber(),String.join("; ",errorList));
                return;
            }
            successCountAto.incrementAndGet();

            // TODO 存数据库 下面业务代码
            System.err.println(" ==========存数据库,业务代码==========");
            System.err.println(" ==========存数据库,业务代码==========");
        });

        result.setSuccessCount(successCountAto.get());
        if (MapUtils.isNotEmpty(errorMap)) {
            List<UserVO> errorRows = Lists.newArrayList();
            errorMap.forEach((lineNo,errors) ->{
                UserImportDTO importDTO = importUserList.get(lineNo - 2);
                UserVO errorRow = new UserVO();
                errorRow.setId(importDTO.getId());
                errorRow.setName(importDTO.getName());
                errorRow.setError(errors);
                errorRows.add(errorRow);
            });
            result.setResultUrl(excelAnalyse.saveFailedExcel(Arrays.asList(errorRows)));
            result.setFailedCount(result.getTotalCount() - result.getSuccessCount());
        }
        return result;
    }
}
