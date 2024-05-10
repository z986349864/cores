package com.zd.core.entity;

import com.zd.core.annotation.LogicDelete;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

public class BasicEntity implements Serializable {

    private static final long serialVersionUID = 1392162988610720992L;
    /**
     * 主键
     */
    @Id
    @Column(name = "id")
    private String id;

    /**
     * 是否有效 0：无效，1有效
     */
    @Column(name = "is_active")
    @LogicDelete(isDeletedValue = 0, notDeletedValue = 1)
    private Integer active;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 创建人
     */
    @Column(name = "create_user_code")
    private String createUserCode;

    /**
     * 修改时间
     */
    @Column(name = "modify_time")
    private Date modifyTime;

    /**
     * 修改人
     */
    @Column(name = "modify_user_code")
    private String modifyUserCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateUserCode() {
        return createUserCode;
    }

    public void setCreateUserCode(String createUserCode) {
        this.createUserCode = createUserCode;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getModifyUserCode() {
        return modifyUserCode;
    }

    public void setModifyUserCode(String modifyUserCode) {
        this.modifyUserCode = modifyUserCode;
    }
}