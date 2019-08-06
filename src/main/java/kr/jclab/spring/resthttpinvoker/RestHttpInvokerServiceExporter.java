package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.jclab.spring.resthttpinvoker.vo.RemoteExceptionData;
import org.springframework.http.MediaType;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import kr.jclab.spring.resthttpinvoker.exception.NotAutoInvokableMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.util.*;

public class RestHttpInvokerServiceExporter extends HttpInvokerServiceExporter {
    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, TypeReference[]> methodTypeMap = new HashMap<>();
    private Set<String> checkSerializableSet = new HashSet<>();
    private boolean hideStackTrace = false;

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected RemoteInvocation readRemoteInvocation(HttpServletRequest request, InputStream is) throws IOException, ClassNotFoundException {
        MediaType mediaType = MediaType.parseMediaType(request.getContentType());
        if(MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            JacksonRemoteInvocationHolder holder = this.objectMapper.readValue(is, JacksonRemoteInvocationHolder.class);
            JacksonRemoteInvocation jacksonRemoteInvocation = new JacksonRemoteInvocation(holder.parse(this.objectMapper, getServiceInterface()), methodTypeMap);
            jacksonRemoteInvocation.setObjectMapper(objectMapper);
            return jacksonRemoteInvocation;
        }
        return super.readRemoteInvocation(request, is);
    }

    @Override
    protected void writeRemoteInvocationResult(HttpServletRequest request, HttpServletResponse response, RemoteInvocationResult result, OutputStream os) throws IOException {
        Map<String, Object> resultPack = new HashMap<>();
        if(result.getValue() == null)
            resultPack.put("value", null);
        else
            resultPack.put("value", Arrays.asList(result.getValue().getClass().getName(), result.getValue()));
        if(result.getException() == null)
            resultPack.put("exception", null);
        else {
            if(result.getException() instanceof InvocationTargetException) {
                Throwable exception = result.getException().getCause();
                if(this.hideStackTrace) {
                    exception.setStackTrace(new StackTraceElement[0]);
                }
                if(checkSerializableSet.contains(exception.getClass().getName())) {
                    Map<String, Object> exceptionData = this.objectMapper.convertValue(exception, Map.class);
                    exceptionData.put("@class", exception.getClass().getName());
                    resultPack.put("exception_orig", exceptionData);
                }else{
                    resultPack.put("exception_red", new RemoteExceptionData(exception));
                }
            }else{
                if(this.hideStackTrace) {
                    result.getException().setStackTrace(null);
                }
                resultPack.put("exception_red", new RemoteExceptionData(result.getException()));
            }
        }
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        this.objectMapper.writeValue(response.getOutputStream(), resultPack);
    }

    public void addMethodParamType(String methodName, TypeReference[] typeReferences) {
        this.methodTypeMap.put(methodName, typeReferences);
    }

    protected void validateMethods() throws NotAutoInvokableMethod {
        for(Method method : getServiceInterface().getDeclaredMethods()) {
            Parameter[] parameters = method.getParameters();
            Class[] throwables = method.getExceptionTypes();
            TypeReference[] preDefinedTypes = methodTypeMap.get(method.getName());
            if(preDefinedTypes == null) {
                for (int i = 0, count = parameters.length; i < count; i++) {
                    Class<?> parameterType = parameters[i].getType();
                    if ((List.class.isAssignableFrom(parameterType)) || (Map.class.isAssignableFrom(parameterType))) {
                        System.out.println(parameters[i].getType());
                        throw new NotAutoInvokableMethod(method.getName() + " method can't auto invoke by " + parameters[i].getName() +
                                "(index: " + i + ", Type: " + parameters[i].getType().getName() + ")");
                    }
                }
                for (int i = 0, count = throwables.length; i < count; i++) {
                    Class<?> throwableClass = throwables[i];
                    if(!this.objectMapper.canSerialize(throwableClass) || !this.objectMapper.canDeserialize(this.objectMapper.constructType(throwableClass))) {
                        throw new NotAutoInvokableMethod(method.getName() + " method can't serialize/deserialize throw class [name=" + throwableClass.getName() + "]");
                    }
                    checkSerializableSet.add(throwableClass.getName());
                }
            }
        }
    }

    @Override
    protected Object getProxyForService() {
        try {
            validateMethods();
        } catch (NotAutoInvokableMethod e) {
            throw new RuntimeException(e);
        }
        return super.getProxyForService();
    }

    public void setHideStackTrace(boolean hideStackTrace) {
        this.hideStackTrace = hideStackTrace;
    }
}
