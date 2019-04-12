package kr.jclab.spring.resthttpinvoker.exception;

public class NotAutoInvokableMethod extends Exception {
    public NotAutoInvokableMethod() {
        super();
    }

    public NotAutoInvokableMethod(String message) {
        super(message);
    }

    public NotAutoInvokableMethod(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAutoInvokableMethod(Throwable cause) {
        super(cause);
    }

    protected NotAutoInvokableMethod(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
