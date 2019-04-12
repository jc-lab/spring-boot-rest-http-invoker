package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.support.RemoteInvocation;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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
    @JsonIgnore
    private Map<String, TypeReference[]> methodTypeMap;

    public JacksonRemoteInvocation(MethodInvocationHolder methodInvocationHolder, Map<String, TypeReference[]> methodTypeMap) {
        super(methodInvocationHolder.methodName, methodInvocationHolder.parameterTypes, methodInvocationHolder.arguments);
        this.methodInvocationHolder = methodInvocationHolder;
        this.methodTypeMap = methodTypeMap;
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
        Parameter[] parameters = method.getParameters();
        Object[] realArguments = new Object[this.methodInvocationHolder.arguments.length];
        TypeReference[] typeReferences = this.methodTypeMap.get(method.getName());
        for(int i=0, count = realArguments.length; i < count; i++) {
            Parameter parameter = parameters[i];
            Object value = this.methodInvocationHolder.arguments[i];
            if(value != null) {
                if(typeReferences != null) {
                    value = this.objectMapper.convertValue(value, typeReferences[i]);
                }else if((value instanceof Map) && (!parameter.getType().isAssignableFrom(value.getClass()))) {
                    value = this.objectMapper.convertValue(value, parameter.getType());
                }
            }
            realArguments[i] = value;
        }
        return method.invoke(targetObject, realArguments);
    }

    public abstract class DynamicTypeReference<T> extends TypeReference
    {
        protected final Type _realType;

        public DynamicTypeReference(Type[] typeList)
        {
            _realType = typeList[0];
        }

        @Override
        public Type getType() { return _realType; }
    }
}
