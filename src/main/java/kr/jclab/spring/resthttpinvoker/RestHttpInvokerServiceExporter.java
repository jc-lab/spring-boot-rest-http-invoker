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

public class RestHttpInvokerServiceExporter extends HttpInvokerServiceExporter {
    private ObjectMapper objectMapper = new ObjectMapper();

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected RemoteInvocation readRemoteInvocation(HttpServletRequest request, InputStream is) throws IOException, ClassNotFoundException {
        MediaType mediaType = MediaType.parseMediaType(request.getContentType());
        if(MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            return this.objectMapper.readValue(is, RemoteInvocation.class);
        }
        return super.readRemoteInvocation(request, is);
    }

    @Override
    protected void writeRemoteInvocationResult(HttpServletRequest request, HttpServletResponse response, RemoteInvocationResult result, OutputStream os) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        this.objectMapper.writeValue(response.getOutputStream(), result);
    }
}
