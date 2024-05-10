package com.zd.core.mq.constant;

public interface MessageConstants {
    interface Business {
        String SYSTEM_ERROR = "common.system.error"; //系统异常

        String ADD_SUCCESS = "common.add.success"; //新增成功
        String UPDATE_SUCCESS = "common.update.success";//修改成功
        String DELETE_SUCCESS = "common.delete.success";//删除成功
        String ENABLE_SUCCESS = "common.enable.success";//启用成功
        String DISABLE_SUCCESS = "common.disable.success";//停用成功
        String OPR_SUCCESS = "common.opr.success"; //新增成功
        String PARAM_ERROR = "common.param.error"; //参数异常
        String DATA_ALREADY_CHANGE = "common.data.already.change"; //数据已变更
        String DATA_NOT_EXIST = "common.data.not.exist"; //数据不存在

    }
}
