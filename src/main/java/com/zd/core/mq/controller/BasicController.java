package com.zd.core.mq.controller;

import com.zd.core.mq.constant.MessageConstants;
import com.zd.core.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicController {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    public Response returnSuccess() {
        return returnSuccess(MessageConstants.Business.OPR_SUCCESS);
    }

    public Response returnSuccess(String successMsg) {
        return returnSuccess(null, successMsg);
    }
    public Response returnSuccess(Object object) {
        return returnSuccess(object, null);
    }
    public Response returnSuccess(Object object, String successMsg) {
        Response response = new Response();
        response.setSuccess(true);
        response.setHasBusinessException(false);
        response.setMessage(successMsg);
        response.setResult(object);
        return response;
    }
}
