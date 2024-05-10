package com.zd.core.mq.failretry.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zd.core.constant.ApplicationConstants;
import com.zd.core.exception.BusinessException;
import com.zd.core.mq.failretry.dao.DefaultFailRetryDao;
import com.zd.core.mq.failretry.dto.FailRetryDto;
import com.zd.core.mq.failretry.entity.FailRetryEntity;
import com.zd.core.mq.failretry.entity.IRetryResultProcessStrategy;
import com.zd.core.mq.failretry.service.IDistributedIdGenerateService;
import com.zd.core.mq.failretry.service.IFailRetryService;
import com.zd.core.mq.failretry.strategy.DefaultNextRetryTimeStrategy;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DefaultFailRetryService implements IFailRetryService {

    @Autowired
    private DefaultFailRetryDao defaultFailRetryDao;
    @Autowired
    private IDistributedIdGenerateService distributedIdGenerateService;

    /**
     * 查询出需要重试的数据
     *
     * @param startTime 要求重试时间-起始
     * @param endTime   要求重试时间-终止
     */
    @Override
    public List<FailRetryEntity> listNeedRetry(int businessType, String startTime, String endTime) {
        return defaultFailRetryDao.listNeedRetry(businessType, startTime, endTime);
    }

    /**
     * 处理重试结果
     *
     * @param failRetryEntity 重试结果
     * @return
     */
    @Override
    @Transactional
    public FailRetryEntity processResult(FailRetryEntity failRetryEntity, IRetryResultProcessStrategy resultProcessStrategy) {
        if (failRetryEntity.getBusinessType() == null
                || StringUtils.isEmpty(failRetryEntity.getBusinessId())
                || failRetryEntity.getStatus() == null) {
            throw new BusinessException("Fail Retry Param Error");
        }
        FailRetryEntity existsEntity = defaultFailRetryDao.queryExistFailEntity(failRetryEntity.getBusinessType(),
                failRetryEntity.getBusinessId());
        if (existsEntity != null) {
            existsEntity.setRetryTimes(existsEntity.getRetryTimes() + 1);
            existsEntity.setModifyUserCode(ApplicationConstants.SYSTEM_OPERATOR);
            existsEntity.setModifyTime(new Date());
            //重发成功
            if (failRetryEntity.getStatus() == ApplicationConstants.SUCCESS) {
                existsEntity.setStatus(ApplicationConstants.SUCCESS);
                defaultFailRetryDao.updateSuccess(existsEntity);
                //成功后处理
                if (resultProcessStrategy != null) {
                    resultProcessStrategy.onSuccess(existsEntity);
                }
            } else {
                //重发再次失败
                existsEntity.setFailReason(failRetryEntity.getFailReason());
                //计算下次发送时间
                if (failRetryEntity.getRetryTimeStrategy() == null) {
                    existsEntity.setRetryTime(new DefaultNextRetryTimeStrategy().calculateNextRetryTime(existsEntity));
                } else {
                    existsEntity.setRetryTime(failRetryEntity.getRetryTimeStrategy().calculateNextRetryTime(existsEntity));
                }
                defaultFailRetryDao.updateFailAgain(existsEntity);
                //判断是否到达最大失败重试次数，如果是，做失败通知处理
                if (existsEntity.getRetryTimes() >= existsEntity.getMaxRetryTimes() && resultProcessStrategy != null) {
                    resultProcessStrategy.onFail(existsEntity);
                }
            }
            return existsEntity;
        }
        if (failRetryEntity.getStatus() == ApplicationConstants.FAIL) {
            //首次处理失败，插入失败表，businessType+businessId唯一索引，重复会忽略
            failRetryEntity.setId(distributedIdGenerateService.generate());
            if (failRetryEntity.getRetryTimes() == null) {
                failRetryEntity.setRetryTimes(0);
            }
            if (failRetryEntity.getRetryTime() == null) {
                //计算下次发送时间
                if (failRetryEntity.getRetryTimeStrategy() == null) {
                    failRetryEntity.setRetryTime(new DefaultNextRetryTimeStrategy().calculateNextRetryTime(failRetryEntity));
                } else {
                    failRetryEntity.setRetryTime(failRetryEntity.getRetryTimeStrategy().calculateNextRetryTime(failRetryEntity));
                }
            }
            failRetryEntity.setCreateTime(new Date());
            failRetryEntity.setCreateUserCode(ApplicationConstants.SYSTEM_OPERATOR);
            failRetryEntity.setModifyTime(new Date());
            failRetryEntity.setModifyUserCode(ApplicationConstants.SYSTEM_OPERATOR);
            defaultFailRetryDao.insertFailRetry(failRetryEntity);
            return failRetryEntity;
        }
        if (failRetryEntity.getStatus() == ApplicationConstants.SUCCESS && resultProcessStrategy != null) {
            resultProcessStrategy.onSuccess(failRetryEntity);
        }
        return failRetryEntity;
    }

    /**
     * 根据重试的开始时间和结束时间 提供重试实体的查询接口
     *
     * @param failRetryDto 重试请求实体
     * @return
     */

    @Override
    public PageInfo<List<FailRetryEntity>> queryFailRetryEntityByTime(FailRetryDto failRetryDto) {
        Integer pageNum = 1;
        Integer pageSize = 10;
        if (failRetryDto.getPage() > 0) {
            pageNum = failRetryDto.getPage();
        }
        if (failRetryDto.getLimit() > 0) {
            pageSize = failRetryDto.getLimit();
        }
        if(failRetryDto.getStartTime()!=null){
            failRetryDto.setStartTime(failRetryDto.getStartTime()/1000);
        }
        if(failRetryDto.getEndTime()!=null){
            failRetryDto.setEndTime(failRetryDto.getEndTime()/1000);
        }
        PageHelper.startPage(pageNum, pageSize, true);
        PageHelper.orderBy("retry_time DESC");
        List<FailRetryEntity> failRetryEntities = defaultFailRetryDao.queryFailRetryEntityByTime(failRetryDto);
        return new PageInfo(failRetryEntities);
    }

    /**
     * 根据主键id更新重试次数
     *
     * @param id
     */
    @Override
    public void updateRetryTimesById(long id) {
        Map<String, Object> data = new HashedMap();
        data.put("id", id);
        data.put("modifyUserCode", ApplicationConstants.SYSTEM_OPERATOR);
        data.put("modifyTime", new Date());
        data.put("retryTime", new Date());
        defaultFailRetryDao.updateRetryTimesById(data);
    }
}
