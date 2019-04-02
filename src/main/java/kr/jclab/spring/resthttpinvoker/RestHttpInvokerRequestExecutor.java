package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;

public class RestHttpInvokerRequestExecutor extends SimpleHttpInvokerRequestExecutor {
    private ObjectMapper objectMapper = new ObjectMapper()
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

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
            List value = (List)result.get("value");
            RemoteInvocationResult remoteInvocationResult = new RemoteInvocationResult();
            remoteInvocationResult.setException((Throwable)result.get("exception"));
            if(value != null) {
                Class returnTypeClazz = null;
                if(value.get(0) != null) {
                    try {
                        returnTypeClazz = Class.forName((String)value.get(0));
                    } catch (ClassNotFoundException e) {
                        returnTypeClazz = originalInvocation.getMethod().getReturnType();
                    }
                }
                remoteInvocationResult.setValue(this.objectMapper.convertValue(value.get(1), returnTypeClazz));
            }
            return remoteInvocationResult;
        }else{
            return this.objectMapper.readValue(responseBody, RemoteInvocationResult.class);
        }
    }
}
