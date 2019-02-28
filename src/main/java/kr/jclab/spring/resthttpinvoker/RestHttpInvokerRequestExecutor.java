package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class RestHttpInvokerRequestExecutor extends SimpleHttpInvokerRequestExecutor {
    private ObjectMapper objectMapper = new ObjectMapper();

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected ByteArrayOutputStream getByteArrayOutputStream(RemoteInvocation invocation) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.objectMapper.writeValue(byteArrayOutputStream, invocation);
        return byteArrayOutputStream;
    }

    @Override
    protected void prepareConnection(HttpURLConnection connection, int contentLength) throws IOException {
        super.prepareConnection(connection, contentLength);
        connection.setRequestProperty(HTTP_HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
    }

    @Override
    protected RemoteInvocationResult readRemoteInvocationResult(InputStream is, String codebaseUrl) throws IOException, ClassNotFoundException {
        return this.objectMapper.readValue(is, RemoteInvocationResult.class);
    }
}
