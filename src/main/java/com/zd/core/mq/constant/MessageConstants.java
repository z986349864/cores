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
        String USER_BATCH_UPLOAD_ERROR_EXPORT_FILE_NAME = "user.batch.upload.error.export.file.name";

        /**
         * 任务调用接口成功
         */
        String JOB_SUCCESS = "job success";
        /**
         * excel import
         */
        String EXCEL_DATA_EMPTY = "excel.data.empty";//数据为空，请检查文件重新上传
        String EXCEL_TOO_LARGE = "excel.too.large";//文件超过指定大小，请检查文件重新上传
        String EXCEL_FORMAT_ERROR = "excel.format.error";//excel文件格式不正确，必须是xlsx结尾的文件
        String EXCEL_ANALYSE_FAILED = "excel.analyse.failed";//导入模板错误，请使用标准模板导入
        String EXCEL_TEMPLATE_INVALID = "excel.template.invalid";//模板版本太旧或不正确，请使用最新模板。
        String EXCEL_FIELD_REQUIRED = "excel.field.required";//存在为空的必填字段
        String EXCEL_FIELD_REPEAT_DATABASE = "excel.field.repeat.database";//{}已存在
        String EXCEL_FIELD_REPEAT_FILE = "excel.field.repeat.file";//上传文件中{}重复
        String EXCEL_FIELD_INVALID = "excel.field.invalid";//Y1, Y2列值无效
        String EXCEL_FIELD_LENGTH_ERROR = "excel.field.length.error";//字段值长度不符合要求
        String EXCEL_FIELD_RANGE_ERROR = "excel.field.range.error";//字段值超出指定范围
    }

    public interface UserMessageConstants {
        String USER_ENROLLMENT_ERROR_NOT_EXISTS = "user.enrollment.error.not.exists";
        String USER_BATCH_UPLOAD_ERROR_EXPORT_FILE_NAME = "user.batch.upload.error.export.file.name";
    }

}
