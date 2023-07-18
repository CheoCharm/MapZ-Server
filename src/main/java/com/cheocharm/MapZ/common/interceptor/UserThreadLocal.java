package com.cheocharm.MapZ.common.interceptor;

import com.cheocharm.MapZ.user.domain.UserEntity;

public class UserThreadLocal {

    private static final ThreadLocal<UserEntity> threadLocal;

    static {
        threadLocal = new ThreadLocal<>();
    }

    public static UserEntity get() {
        return threadLocal.get();
    }

    public static void set(UserEntity userEntity) {
        threadLocal.set(userEntity);
    }

    public static void remove() {
        threadLocal.remove();
    }

}
