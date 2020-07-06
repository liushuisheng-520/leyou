package com.leyou.common.auth.entity;


public class UserHolder {

    //用threadlocal保存UserId变量
    private static ThreadLocal<String> threadLocal=new ThreadLocal<String>();


    public static String getUserId() {
        return threadLocal.get() ;
    }

    public static void setUserId(String userId) {
        threadLocal.set(userId);
    }

    public static void removeUserId(){
        threadLocal.remove();
    }
}
