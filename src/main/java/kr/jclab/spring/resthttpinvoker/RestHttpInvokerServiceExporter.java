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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestHttpInvokerServiceExporter extends HttpInvokerServiceExporter {
    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, TypeReference[]> methodTypeMap = new HashMap<>();

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
            RemoteExceptionData remoteExceptionData = new RemoteExceptionData((result.getException() instanceof InvocationTargetException) ? result.getException().getCause() : result.getException());
            resultPack.put("exception", remoteExceptionData);
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
}
