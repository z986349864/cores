package com.zd.core.test.cache;

import com.zd.core.cache.ICacheDataProvider;
import com.zd.core.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class UserAllCacheProvider implements ICacheDataProvider<String, List<User>> {


    Logger logger = LoggerFactory.getLogger(UserAllCacheProvider.class);

    /**
     * 可以调用别的微服务，确保最终一致性Proxy 。现在为测试
    * */

    @Override
    public List<User> get(String key) {
        User user = new User();
        user.setName("ceshi11");
        user.setId(111);
        User user1 = new User();
        user1.setName("ceshi222");
        user1.setId(222);
        List<User> userList = Arrays.asList(user);
        userList.add(user1);
        /**
         * 判断调用结果是否正确，正确返回，异常打印日志即可。
         * 通过xxljob 来处理失败消息。如重发5次还是失败则人工介入
        */
        return userList;
    }
}