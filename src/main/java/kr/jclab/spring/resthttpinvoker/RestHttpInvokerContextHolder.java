package kr.jclab.spring.resthttpinvoker;

public class RestHttpInvokerContextHolder {
    private static final ThreadLocal<RestHttpInvokerContext> threadLocal = new ThreadLocal<>();

    public static RestHttpInvokerContext getContext() {
        return threadLocal.get();
    }

    public static void setContext(RestHttpInvokerContext context) {
        threadLocal.set(context);
    }

    public static void removeContext() {
        threadLocal.remove();
    }
}
