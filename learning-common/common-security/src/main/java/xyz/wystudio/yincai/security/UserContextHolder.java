package xyz.wystudio.yincai.security;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserContextHolder {

    private static final ThreadLocal<CurrentUser> CONTEXT = new ThreadLocal<>();

    public static void set(CurrentUser user) {
        CONTEXT.set(user);
    }

    public static CurrentUser get() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        CurrentUser user = get();
        return user != null ? user.getUserId() : null;
    }

    public static Long getTenantId() {
        CurrentUser user = get();
        return user != null ? user.getTenantId() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
