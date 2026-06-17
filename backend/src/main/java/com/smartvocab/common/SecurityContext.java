package com.smartvocab.common;

/**
 * 当前登录用户上下文（基于 ThreadLocal）
 */
public class SecurityContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> EMAIL = new ThreadLocal<>();

    public static void setUserId(Long id) { USER_ID.set(id); }
    public static Long getUserId() { return USER_ID.get(); }
    public static void setEmail(String e) { EMAIL.set(e); }
    public static String getEmail() { return EMAIL.get(); }
    public static void clear() { USER_ID.remove(); EMAIL.remove(); }
}
