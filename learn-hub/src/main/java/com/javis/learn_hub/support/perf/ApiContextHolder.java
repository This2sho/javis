package com.javis.learn_hub.support.perf;

public class ApiContextHolder {

    private static final ThreadLocal<ApiContext> context = new ThreadLocal<>();

    public static void set(ApiContext apiContext) {
        context.set(apiContext);
    }

    public static ApiContext get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}
