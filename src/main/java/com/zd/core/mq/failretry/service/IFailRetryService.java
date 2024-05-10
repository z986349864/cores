package com.zd.core.mq.failretry.service;

import com.github.pagehelper.PageInfo;
import com.zd.core.mq.failretry.dto.FailRetryDto;
import com.zd.core.mq.failretry.entity.FailRetryEntity;
import com.zd.core.mq.failretry.entity.IRetryResultProcessStrategy;

import java.util.List;

public interface IFailRetryService {

    /**
     * 查询出需要重试的数据
     * @param businessType  重试的业务类型
     * @param startTime     要求重试时间-起始
     * @param endTime       要求重试时间-终止
     */
    List<FailRetryEntity> listNeedRetry(int businessType, String startTime, String endTime);

    /**
     * 处理重试结果
     * @param failRetryEntity   重试结果
     * @param resultProcessStrategy   重试结果处理策略
     */
    FailRetryEntity processResult(FailRetryEntity failRetryEntity, IRetryResultProcessStrategy resultProcessStrategy);

    /**
     * 分页查询 重试时间在在指定范围内的数据
     *
     * @param failRetryDto   重试请求实体
     */
    PageInfo<List<FailRetryEntity>> queryFailRetryEntityByTime(FailRetryDto failRetryDto);

    /**
     * 根据主键id  更新重试次数
     *
     * @param id
     */
    void updateRetryTimesById(long id);
}
