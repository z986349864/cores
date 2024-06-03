package com.zd.core.entity;


import com.zd.core.constant.ApplicationConstants;
import com.zd.core.context.UserContext;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 通用po初始化
 */
@Component
public class BasicEntityInitializer {

    /**
     * 初始化新增参数
     *
     * @param basicEntity
     * @param date
     */
    public void initAdd(BasicEntity basicEntity, Date date) {
        basicEntity.setActive(ApplicationConstants.ACTIVE);
        basicEntity.setCreateTime(date);
        basicEntity.setModifyTime(date);
        basicEntity.setCreateUserCode(UserContext.getCurrentUserCode());
        basicEntity.setModifyUserCode(UserContext.getCurrentUserCode());
    }

    /**
     * 初始化新增参数
     *
     * @param basicEntity
     */
    public void initAdd(BasicEntity basicEntity) {
        initAdd(basicEntity, new Date());
    }

    /**
     * 初始化新增参数
     * @param basicEntity
     * @param date
     * @param userCode
     */
    public void initAdd(BasicEntity basicEntity, Date date , String userCode) {
        basicEntity.setActive(ApplicationConstants.ACTIVE);
        basicEntity.setCreateTime(date);
        basicEntity.setModifyTime(date);
        basicEntity.setCreateUserCode(userCode);
        basicEntity.setModifyUserCode(userCode);
    }

    /**
     * 初始化新增参数
     *
     * @param basicEntity
     */
    public void initModify(BasicEntity basicEntity) {
        Date date = new Date();
        basicEntity.setModifyTime(date);
        basicEntity.setModifyUserCode(UserContext.getCurrentUserCode());

    }

    /**
     * 初始化修改参数
     *
     * @param basicEntity
     */
    public void initModify(BasicEntity basicEntity, Date date) {
        basicEntity.setModifyTime(date);
        basicEntity.setModifyUserCode(UserContext.getCurrentUserCode());
    }

    /**
     * 初始化修改参数
     *0
     * @param basicEntity
     */
    public void initModify(BasicEntity basicEntity, Date date , String userCode) {
        basicEntity.setModifyTime(date);
        basicEntity.setModifyUserCode(userCode);
    }

    /**
     * 批量初始化
     *
     * @param baseEntities
     */
    public void batchInitAdd(List<? extends BasicEntity> baseEntities) {
        Date date = new Date();
        baseEntities.forEach(basicEntity -> initAdd(basicEntity, date));
    }

    /**
     * 批量初始化
     *
     * @param baseEntities
     */
    public void batchInitAdd(List<? extends BasicEntity> baseEntities, Date date) {
        baseEntities.forEach(basicEntity -> initAdd(basicEntity, date));
    }

}