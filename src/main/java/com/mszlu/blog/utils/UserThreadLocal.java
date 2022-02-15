package com.mszlu.blog.utils;

import com.mszlu.blog.dao.pojo.SysUser;

public class UserThreadLocal {

private UserThreadLocal(){}
//线程变量隔离
private static final ThreadLocal<SysUser> LOCAL = new ThreadLocal<>();
// 放
public static void put(SysUser sysUser){
LOCAL.set(sysUser);
}

// 取
public static SysUser get(){
return LOCAL.get();
}

// 清除
public static void remove(){
LOCAL.remove();
}
}