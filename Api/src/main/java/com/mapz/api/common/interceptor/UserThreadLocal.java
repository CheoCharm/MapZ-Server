package com.mapz.api.common.interceptor;

import com.mapz.domain.domains.user.entity.User;

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
