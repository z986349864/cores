package com.zd.core.mq.failretry.dao;

import com.zd.core.mq.failretry.dto.FailRetryDto;
import com.zd.core.mq.failretry.entity.FailRetryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DefaultFailRetryDao {

    /**
     * 查询出需要重试的记录
     *
     * @param businessType
     * @param startTime
     * @param endTime
     * @return
     */
    List<FailRetryEntity> listNeedRetry(@Param("businessType") int businessType,
                                        @Param("startTime") long startTime,
                                        @Param("endTime") long endTime);

    /**
     * 查询出是已存在的失败重试记录
     * @param businessType
     * @param businessId
     * @return
     */
    FailRetryEntity queryExistFailEntity(@Param("businessType") Integer businessType,
                                         @Param("businessId") String businessId);

    /**
     * 保存失败重试记录
     * @param failRetryEntity
     */
    void insertFailRetry(FailRetryEntity failRetryEntity);

    /**
     * 按id更新为处理成功
     * @param failRetryEntity
     */
    void updateSuccess(FailRetryEntity failRetryEntity);

    /**
     * 按照id更新为再次处理失败
     * @param failRetryEntity
     */
    void updateFailAgain(FailRetryEntity failRetryEntity);

    /**
     * 查询出需要重试的记录
     *
     * @param failRetryDto
     * @return
     */
    List<FailRetryEntity> queryFailRetryEntityByTime(FailRetryDto failRetryDto);

    /**
     * 根据主键ID更新重试次数
     *
     * @param map
     */
    void updateRetryTimesById(Map<String, Object> map);
}
