package com.zd.core.test.controller;

import com.zd.core.mq.consumer.IConsumerRetryService;
import com.zd.core.mq.controller.BasicController;
import com.zd.core.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 失败重试controller
 */
@RestController
@RequestMapping("/consumer/fail-retry")
public class ConsumerFailRetryController extends BasicController {

    @Autowired
    private IConsumerRetryService consumerRetryService;

    @GetMapping("/v1/businessType/{failType}")
    public Response consumerFailRetry(@RequestParam("lastSendTime") String lastSendTime, @RequestParam("thisTime") String thisTime, @PathVariable("failType") Integer failType) {
        consumerRetryService.retry(lastSendTime, thisTime, failType);
        return returnSuccess();
    }

}
