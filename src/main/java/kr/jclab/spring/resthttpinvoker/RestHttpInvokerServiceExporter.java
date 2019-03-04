package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RestHttpInvokerServiceExporter extends HttpInvokerServiceExporter {
    private ObjectMapper objectMapper = new ObjectMapper();

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected RemoteInvocation readRemoteInvocation(HttpServletRequest request, InputStream is) throws IOException, ClassNotFoundException {
        MediaType mediaType = MediaType.parseMediaType(request.getContentType());
        if(MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            JacksonRemoteInvocationHolder holder = this.objectMapper.readValue(is, JacksonRemoteInvocationHolder.class);
            JacksonRemoteInvocation jacksonRemoteInvocation = new JacksonRemoteInvocation(holder.parse(this.objectMapper, getServiceInterface()));
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
        else
            resultPack.put("exception", Arrays.asList(result.getException().getClass().getName(), result.getException()));
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        this.objectMapper.writeValue(response.getOutputStream(), resultPack);
    }
}
