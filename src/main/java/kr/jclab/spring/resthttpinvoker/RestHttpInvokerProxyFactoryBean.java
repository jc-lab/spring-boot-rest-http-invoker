package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.http.client.HttpClient;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import java.io.IOException;

public class RestHttpInvokerProxyFactoryBean extends HttpInvokerProxyFactoryBean {
    private ObjectMapper objectMapper = new ObjectMapper();
    private RestHttpInvokerRequestExecutor executor = new RestHttpInvokerRequestExecutor();
    private RemoteInvokeRetryHandler remoteInvokeRetryHandler = null;

    public RestHttpInvokerProxyFactoryBean() {
        super();
        setHttpInvokerRequestExecutor(executor);
    }

    public void setHttpClient(HttpClient httpClient) {
        executor.setHttpClient(httpClient);
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        executor.setObjectMapper(objectMapper);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        super.setBeanClassLoader(classLoader);
        executor.setBeanClassLoader(classLoader);
    }

    @Override
    protected RemoteInvocationResult executeRequest(RemoteInvocation invocation, MethodInvocation originalInvocation) throws Exception {
        int count = 0;
        RemoteInvocationResult result = null;
        IOException ioException = null;
        do {
            try {
                result = executor.jacksonExecuteRequest(this, invocation, originalInvocation);
                if(!result.hasException())
                    break;
            }catch (IOException e) {
                ioException = e;
            }
            count++;
        } while (checkRetry(count, ioException, (result != null) ? result.getException() : null));
        if(result == null && ioException != null)
            throw ioException;
        return result;
    }

    private boolean checkRetry(int count, IOException ioException, Throwable remoteThrowable) {
        if(this.remoteInvokeRetryHandler == null)
            return false;
        return remoteInvokeRetryHandler.checkRetry(count, ioException, remoteThrowable);
    }

    @Override
    protected RemoteInvocationResult executeRequest(RemoteInvocation invocation) throws Exception {
        return this.executeRequest(invocation, null);
    }

    public void setRemoteInvokeRetryHandler(RemoteInvokeRetryHandler remoteInvokeRetryHandler) {
        this.remoteInvokeRetryHandler = remoteInvokeRetryHandler;
    }

    public void setHttpClientRequestCustomizer(HttpClientRequestCustomizer httpClientRequestCustomizer) {
        executor.setHttpClientRequestCustomizer(httpClientRequestCustomizer);
    }

}
