package kr.jclab.spring.resthttpinvoker.exception;

import kr.jclab.spring.resthttpinvoker.vo.RemoteExceptionData;

public class RemoteException extends RuntimeException {
    public RemoteException(Throwable cause) {
        super(cause);
    }

    private RemoteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static RemoteException create(RemoteExceptionData remoteExceptionData) {
        RemoteException remoteException = new RemoteException(remoteExceptionData.getMessage(), null, false, true);
        StackTraceElement[] stackTraceElementList;
        if(remoteExceptionData.getStackTrace() != null) {
            int i = 0;
            stackTraceElementList = new StackTraceElement[remoteExceptionData.getStackTrace().size()];
            for(RemoteExceptionData.StackTraceElementData stackTraceElementData : remoteExceptionData.getStackTrace()) {
                stackTraceElementList[i++] = new StackTraceElement(stackTraceElementData.getDeclaringClass(), stackTraceElementData.getMethodName(), stackTraceElementData.getFileName(), stackTraceElementData.getLineNumber());
            }
        }else{
            stackTraceElementList = new StackTraceElement[0];
        }
        remoteException.setStackTrace(stackTraceElementList);
        return remoteException;
    }
}
