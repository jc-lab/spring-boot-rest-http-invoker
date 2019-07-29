package kr.jclab.spring.resthttpinvoker.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class RemoteExceptionData {
    @JsonProperty("message")
    private final String message;

    @JsonProperty("declaring_class")
    private final String declaringClass;

    @JsonProperty("stack_trace")
    private final List<StackTraceElementData> stackTrace;

    @JsonProperty("cause")
    private final RemoteExceptionData cause;

    public RemoteExceptionData(Throwable throwable) {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        this.message = throwable.getMessage();
        this.declaringClass = throwable.getClass().getName();
        this.stackTrace = new ArrayList<>();
        for(StackTraceElement stackTraceElement : stackTraceElements) {
            this.stackTrace.add(new StackTraceElementData(stackTraceElement));
        }
        if(throwable.getCause() != null) {
            this.cause = new RemoteExceptionData(throwable.getCause());
        }else{
            this.cause = null;
        }
    }

    @JsonCreator
    public RemoteExceptionData(
            @JsonProperty("message") String message,
            @JsonProperty("declaring_class") String declaringClass,
            @JsonProperty("stack_trace") List<StackTraceElementData> stackTrace,
            @JsonProperty("cause") RemoteExceptionData cause) {
        this.message = message;
        this.declaringClass = declaringClass;
        this.stackTrace = stackTrace;
        this.cause = cause;
    }

    public String getMessage() {
        return message;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public List<StackTraceElementData> getStackTrace() {
        return stackTrace;
    }

    public RemoteExceptionData getCause() {
        return cause;
    }

    public static class StackTraceElementData {
        @JsonProperty("declaring_class")
        private final String declaringClass;
        @JsonProperty("method_name")
        private final String methodName;
        @JsonProperty("file_name")
        private final String fileName;
        @JsonProperty("line_number")
        private final int    lineNumber;

        public StackTraceElementData(StackTraceElement element) {
            this.declaringClass = element.getClassName();
            this.methodName = element.getMethodName();
            this.fileName = element.getFileName();
            this.lineNumber = element.getLineNumber();
        }

        @JsonCreator
        public StackTraceElementData(
                @JsonProperty("declaring_class") String declaringClass,
                @JsonProperty("method_name") String methodName,
                @JsonProperty("file_name") String fileName,
                @JsonProperty("line_number") int lineNumber) {
            this.declaringClass = declaringClass;
            this.methodName = methodName;
            this.fileName = fileName;
            this.lineNumber = lineNumber;
        }

        public String getDeclaringClass() {
            return declaringClass;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getFileName() {
            return fileName;
        }

        public int getLineNumber() {
            return lineNumber;
        }
    }
}
