package kr.jclab.spring.resthttpinvoker;

import org.springframework.remoting.support.RemoteInvocationResult;

public class RestHttpRemoteInvocationResult extends RemoteInvocationResult {
    public RestHttpRemoteInvocationResult(Object value) {
        super(value);
    }

    public RestHttpRemoteInvocationResult(Throwable exception) {
        super(exception);
    }

    public RestHttpRemoteInvocationResult() {
    }

    @Override
    public boolean hasInvocationTargetException() {
        return true;
    }
}
