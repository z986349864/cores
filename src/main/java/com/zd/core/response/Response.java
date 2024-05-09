package com.zd.core.response;

import java.io.Serializable;

public class Response<T> implements Serializable {

    /**
     * 要求前端进行重定向的编码
     */
    public static final String ERROR_REDIRECT = "302";

    /**
     * 无权访问指定资源
     */
    public static final String ERROR_CODE_NOT_RIGHT_TO_ACCESS = "403";

    /**
     * 校验类异常编码
     */
    public static final String ERROR_CODE_VALIDATE = "10000";

    /**
     * 业务异常类异常编码
     */
    public static final String ERROR_CODE_BUSINESS_EXCEPTION = "20000";

    /**
     * 未捕获类异常编码
     */
    public static final String ERROR_CODE_UNHANDLED_EXCEPTION = "90000";

    /**
     * 请求id，系统异常时需要将此参数传递到前台去
     */
    private String requestId;

    /**
     * 请求是否处理成功
     */
    private boolean success;

    /**
     * 返回结果有业务异常
     */
    private boolean hasBusinessException;

    /**
     * 业务异常错误代码
     */
    private String errorCode;

    /**
     * 业务异常错误信息
     */
    private String errorMsg;

    /**
     * 提示消息，需要进行国际化
     */
    private String message;

    /**
     * 正常返回参数
     */
    private T result;

    /**
     * 是否降级
     */
    private boolean isFallback;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isHasBusinessException() {
        return hasBusinessException;
    }

    public void setHasBusinessException(boolean hasBusinessException) {
        this.hasBusinessException = hasBusinessException;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public boolean isFallback() {
        return isFallback;
    }

    public void setFallback(boolean fallback) {
        isFallback = fallback;
    }

    /**
     * 构建无权访问的返回实体
     *
     * @param errorMessage
     * @return
     */
    public static Response buildNoRightToAccessResponse(String errorMessage) {
        Response result = new Response();
        result.setSuccess(false);
        result.setHasBusinessException(true);
        result.setErrorCode(Response.ERROR_CODE_NOT_RIGHT_TO_ACCESS);
        result.setErrorMsg(errorMessage);
        result.setMessage(errorMessage);
        return result;
    }


    /**
     * 返回不带结果的成功响应
     */
    public static <T> Response<T> success() {
        Response<T> response = new Response<>();
        response.setSuccess(true);
        response.setHasBusinessException(false);
        return response;
    }


    /**
     * 返回带结果的成功响应
     */
    public static <T> Response<T> success(T result) {
        Response<T> response = new Response<>();
        response.setSuccess(true);
        response.setHasBusinessException(false);
        response.setResult(result);
        return response;
    }

    /**
     * 返回带结果及提示信息的成功响应
     */
    public static <T> Response<T> success(T result, String successMsg) {
        Response<T> response = new Response<>();
        response.setSuccess(true);
        response.setHasBusinessException(false);
        response.setMessage(successMsg);
        response.setResult(result);
        return response;
    }

}