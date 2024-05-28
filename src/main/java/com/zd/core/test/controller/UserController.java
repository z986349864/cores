package com.zd.core.test.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.zd.core.annotation.RedisCache;
import com.zd.core.config.BusinessQueueProperties;
import com.zd.core.constant.MessageConstants;
import com.zd.core.excel.service.UserService;
import com.zd.core.lock.IDistributedLockExecutor;
import com.zd.core.model.User;
import com.zd.core.mq.constant.FailRetryType;
import com.zd.core.mq.controller.BasicController;
import com.zd.core.mq.producer.IMessageProducer;
import com.zd.core.mq.producer.RabbitMessage;
import com.zd.core.response.Response;
import com.zd.core.test.cache.UserAllCache;
import groovy.util.logging.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

@Log4j
@RestController
@RequestMapping("/")
public class UserController extends BasicController {

    @Autowired
    private UserAllCache userAllCache;

    @Autowired
    private IDistributedLockExecutor distributedLockExecutor;

    @Autowired
    private IMessageProducer msgProducer;

    @Autowired
    private BusinessQueueProperties queueProperties;

    @Autowired
    private UserService userService;

   // 批量上传
    @PostMapping("/batch-upload")
    public Response<Boolean> batchUploadHsCode(@RequestPart MultipartFile file) {
        return returnSuccess(userService.batchUploadUserList(file));
    }

    // 导出
    @GetMapping("/exportUser")
    public void exportUser() {
        List<User> userList = userAllCache.get("ALL");
        //数据导出
        export(userList, "用户列表", "用户列表");

    }

    @RedisCache
    @GetMapping("/getCache")
    public User getCache() {
        User user = new User();
        user.setName("ceshi22");
        user.setId(33);
        return user;
    }

    @GetMapping("/findUserTest")
    public Response getUserTest() {
        List<User> userList = userAllCache.get("ALL");
        String s = JSONObject.toJSONString(userList);
        distributedLockExecutor.runWithNoLeaseTimeLocked(() -> {
            System.err.println("..............需要上锁的业务代码块..........");
        }, String.join("redis:key"));
        return returnSuccess(s, MessageConstants.COMMON_USER_FIND_SUCCESS_MESSAGE);
    }

    @GetMapping("/getUserAll")
    public String getUserAll() {
        List<User> userList = userAllCache.get("ALL");
        String s = JSONObject.toJSONString(userList);
        return s;
    }

    @GetMapping("/sendMsg")
    public String sendMsg() {
        String id = UUID.randomUUID().toString();
        List<User> userList = userAllCache.get("ALL");
        RabbitMessage<List<User>> msg = new RabbitMessage<>(
                id,
                FailRetryType.USER_ALL_FAIL,
                userList,
                queueProperties.getExchangeName(),
                queueProperties.getUserRoutingKey()
        );
        msgProducer.sendMessageAfterTransactionCommitted(msg);

        return "success";
    }


    /**
     * sku商品列表导出
     * @param userList
     */
    private <T> void export(List<T> userList,String fileName,String sheetName) {
        try {
            if(CollectionUtils.isEmpty(userList)){
                return;
            }
            //获取泛型参数类型
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletResponse response = servletRequestAttributes.getResponse();
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");


            EasyExcel.write(response.getOutputStream(), userList.get(0).getClass()).sheet(sheetName).doWrite(userList);
        }catch (Exception e){
            log.error("user queryList export error",e);
        }
    }

}
