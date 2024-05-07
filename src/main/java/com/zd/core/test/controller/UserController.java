package com.zd.core.test.controller;

import com.zd.core.cache.CacheKey;
import com.zd.core.model.User;
import com.zd.core.test.cache.UserAllCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * test
 */
@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserAllCache userAllCache;

    @RequestMapping("/getUserAll")
    public List<User> getUserAll() {
        String userId = "test";
        String userName = "zhangSan";
        List<User> userList = userAllCache.get(new CacheKey(userId, userName).generateKey());
        return userList;
    }

    @RequestMapping("/getUserAll1")
    public List<User> getUserAll1() {
        String userId = "test";
        String userName = "zhangSan";
        List<User> userList = userAllCache.get(new CacheKey(userId, userName).generateKey());
        return userList;
    }
}
