package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.internal.org.objectweb.asm.TypeReference;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.http.MediaType;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;

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



    public RemoteInvocationResult jacksonExecuteRequest(HttpInvokerClientConfiguration config, RemoteInvocation invocation, MethodInvocation originalInvocation) throws IOException {
        HttpURLConnection con = openConnection(config);
        ByteArrayOutputStream baos = getByteArrayOutputStream(invocation);
        prepareConnection(con, baos.size());
        writeRequestBody(config, con, baos);
        validateResponse(config, con);
        InputStream responseBody = readResponseBody(config, con);
        if(originalInvocation != null) {
            Map<String, Object> result = this.objectMapper.readValue(responseBody, Map.class);
            Object value = result.get("value");
            RemoteInvocationResult remoteInvocationResult = new RemoteInvocationResult();
            remoteInvocationResult.setException((Throwable)result.get("exception"));
            if(value != null)
                remoteInvocationResult.setValue(this.objectMapper.convertValue(value, originalInvocation.getMethod().getReturnType()));
            return remoteInvocationResult;
        }else{
            return this.objectMapper.readValue(responseBody, RemoteInvocationResult.class);
        }
    }
}
