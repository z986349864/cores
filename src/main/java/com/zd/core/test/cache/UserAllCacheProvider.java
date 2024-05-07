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
/*
    @Autowired
    private InvokerProxy invokerProxy;*/

    @Override
    public List<User> get(String key) {
        User user = new User();
        user.setName("ceshi11");
        user.setId(22);
        List<User> userList = Arrays.asList(user);
        /*Response<List<DataCenterConfigDTO>> response = invokerProxy.getAll();
        if (response.isSuccess() && !CollectionUtils.isEmpty(response.getResult())) {
            return response.getResult();
        }
        logger.error("AllDataCenterConfigCacheProvider  get error:" + JSONObject.toJSONString(response));
        */
        return userList;
    }
}