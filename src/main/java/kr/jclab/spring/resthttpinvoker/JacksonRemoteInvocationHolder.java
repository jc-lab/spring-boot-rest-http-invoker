package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JacksonRemoteInvocationHolder {
    public String methodName;
    public String[] parameterTypes;
    public Object[] arguments;
    public Map<String, Serializable> attributes;

    public JacksonRemoteInvocationHolder() {}

    public JacksonRemoteInvocation.MethodInvocationHolder parse(ObjectMapper objectMapper, Class clazz) {
        JacksonRemoteInvocation.MethodInvocationHolder methodInvocationHolder = new JacksonRemoteInvocation.MethodInvocationHolder();
        List<Method> foundMethod = new ArrayList<>();
        Object[] argumentsObjects = new Object[this.arguments.length];
        methodInvocationHolder.methodName = this.methodName;

        for(Method method : clazz.getMethods()) {
            if(method.getName().equals(this.methodName)) {
                foundMethod.add(method);
            }
        }

        if(foundMethod.size() == 1) {
            int i;
            methodInvocationHolder.method = foundMethod.get(0);
            methodInvocationHolder.parameterTypes = methodInvocationHolder.method.getParameterTypes();
            if(this.arguments.length != methodInvocationHolder.parameterTypes.length) {
                throw new RuntimeException("Invocation failed");
            }
            i = 0;
            for(Class parameterClazz : methodInvocationHolder.parameterTypes) {
                argumentsObjects[i] = objectMapper.convertValue(this.arguments[i], parameterClazz);
                i++;
            }
            methodInvocationHolder.arguments = argumentsObjects;
        }else{
            throw new RuntimeException("Invocation failed");
        }

        return methodInvocationHolder;
    }
}
