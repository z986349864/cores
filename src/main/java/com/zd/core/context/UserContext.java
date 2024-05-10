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
     * @author 陈宇霖
     * @date 2017年08月02日19:11:11
     */
    public static IUser getCurrentUser() {
        return USER_HOLDER.get();
    }

    /**
     * 设置当前登陆用户
     * @param user
     * @author 陈宇霖
     * @date 2017年08月02日20:31:00
     */
    public static void setCurrentUser(IUser user) {
        USER_HOLDER.set(user);
    }

    /**
     * 清除用户
     * @author 陈宇霖
     * @date 2017年08月02日20:30:54
     */
    public static void remove() {
        USER_HOLDER.remove();
    }

    /**
     * 获取当前操作用户名
     * @return
     * @author 陈宇霖
     * @date 2019年04月07日14:50:32
     */
    public static String getCurrentUserCode() {
        IUser user = getCurrentUser();
        if (user == null) {
            return ApplicationConstants.SYSTEM_OPERATOR;
        }
        return user.getUserCode();
    }
}
