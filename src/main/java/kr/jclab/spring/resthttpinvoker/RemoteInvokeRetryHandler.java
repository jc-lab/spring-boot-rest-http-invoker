package kr.jclab.spring.resthttpinvoker;

import java.io.IOException;

public interface RemoteInvokeRetryHandler {
    boolean checkRetry(int count, IOException ioException, Throwable remoteThrowable);
}
