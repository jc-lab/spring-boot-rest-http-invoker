package kr.jclab.spring.resthttpinvoker;

public interface RemoteInvokeRetryHandler {
    boolean checkRetry(int count);
}
