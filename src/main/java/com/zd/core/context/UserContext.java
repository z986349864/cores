package com.zd.core.context;

import com.zd.core.constant.ApplicationConstants;
import com.zd.core.entity.IUser;

public class UserContext {

    /**
     * 用threadLocal存储当前登陆的用户
     */
    private static final ThreadLocal<IUser> USER_HOLDER = new ThreadLocal<IUser>();

    /**
     * 获取当前登录用户
     * @return
     */
    public static IUser getCurrentUser() {
        return USER_HOLDER.get();
    }

    /**
     * 设置当前登陆用户
     * @param user
     */
    public static void setCurrentUser(IUser user) {
        USER_HOLDER.set(user);
    }

    /**
     * 清除用户
     */
    public static void remove() {
        USER_HOLDER.remove();
    }

    /**
     * 获取当前操作用户名
     * @return
     */
    public static String getCurrentUserCode() {
        IUser user = getCurrentUser();
        if (user == null) {
            return ApplicationConstants.SYSTEM_OPERATOR;
        }
        return user.getUserCode();
    }
}
