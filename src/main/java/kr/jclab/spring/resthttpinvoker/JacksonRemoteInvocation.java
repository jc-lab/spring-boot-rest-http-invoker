package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.support.RemoteInvocation;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class JacksonRemoteInvocation extends RemoteInvocation {
    public static class MethodInvocationHolder {
        public String methodName;
        public Method method;
        public Class<?>[] parameterTypes;
        public Object[] arguments;

        public MethodInvocationHolder() {}

        public MethodInvocationHolder(String methodName, Method method, Class<?>[] parameterTypes, Object[] arguments) {
            this.methodName = methodName;
            this.method = method;
            this.parameterTypes = parameterTypes;
            this.arguments = arguments;
        }
    }

    @JsonIgnore
    private ObjectMapper objectMapper = null;
    @JsonIgnore
    private MethodInvocationHolder methodInvocationHolder;

    public JacksonRemoteInvocation(MethodInvocation methodInvocation) {
        super(methodInvocation);
        this.methodInvocationHolder = new MethodInvocationHolder(methodInvocation.getMethod().getName(), methodInvocation.getMethod(), methodInvocation.getMethod().getParameterTypes(), methodInvocation.getArguments());
    }

    public JacksonRemoteInvocation(MethodInvocationHolder methodInvocationHolder) {
        super(methodInvocationHolder.methodName, methodInvocationHolder.parameterTypes, methodInvocationHolder.arguments);
        this.methodInvocationHolder = methodInvocationHolder;
    }

    public JacksonRemoteInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments) {
        super(methodName, parameterTypes, arguments);
        this.methodInvocationHolder = new MethodInvocationHolder(methodName, null, parameterTypes, arguments);
    }

    public JacksonRemoteInvocation() {
        super();
        this.methodInvocationHolder = new MethodInvocationHolder();
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void setMethodName(String methodName) {
        super.setMethodName(methodName);
        this.methodInvocationHolder.methodName = methodName;
    }

    @Override
    public void setParameterTypes(Class<?>[] parameterTypes) {
        super.setParameterTypes(parameterTypes);
        this.methodInvocationHolder.parameterTypes = parameterTypes;
    }

    @Override
    public void setArguments(Object[] arguments) {
        super.setArguments(arguments);
        this.methodInvocationHolder.arguments = arguments;
    }

    @Override
    public void setAttributes(Map<String, Serializable> attributes) {
        super.setAttributes(attributes);
    }

    @Override
    public Object invoke(Object targetObject) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = targetObject.getClass().getMethod(this.methodInvocationHolder.methodName, this.methodInvocationHolder.parameterTypes);
        Class<?>[] methodParameterTypes = method.getParameterTypes();
        Object[] realArguments = new Object[this.methodInvocationHolder.arguments.length];
        for(int i=0, count = realArguments.length; i < count; i++) {
            Object value = this.methodInvocationHolder.arguments[i];
            Class<?> parameterType = methodParameterTypes[i];
            if(!parameterType.isAssignableFrom(value.getClass())) {
                if(value instanceof Map) {
                    value = this.objectMapper.convertValue(value, parameterType);
                }
            }
            realArguments[i] = value;
        }
        return method.invoke(targetObject, realArguments);
    }
}
