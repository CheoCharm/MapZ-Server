package com.cheocharm.MapZ.common.interceptor;

import com.cheocharm.MapZ.user.domain.User;

public class UserThreadLocal {

    private static final ThreadLocal<User> threadLocal;

    static {
        threadLocal = new ThreadLocal<>();
    }

    public static User get() {
        return threadLocal.get();
    }

    public static void set(User user) {
        threadLocal.set(user);
    }

    public static void remove() {
        threadLocal.remove();
    }

}
