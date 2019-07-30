package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.http.client.HttpClient;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

public class RestHttpInvokerProxyFactoryBean extends HttpInvokerProxyFactoryBean {
    private ObjectMapper objectMapper = new ObjectMapper();
    private final RestHttpInvokerRequestExecutor executor = new RestHttpInvokerRequestExecutor();
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
        return executor.jacksonExecuteRequest(this, invocation, originalInvocation);
    }

    @Override
    protected RemoteInvocationResult executeRequest(RemoteInvocation invocation) throws Exception {
        int count = 0;
        RemoteInvocationResult result;
        do {
            result = this.executeRequest(invocation, null);
            count++;
        } while (result.hasException() && checkRetry(count));
        return result;
    }

    private boolean checkRetry(int count) {
        if(this.remoteInvokeRetryHandler == null)
            return false;
        return remoteInvokeRetryHandler.checkRetry(count);
    }

    public void setRemoteInvokeRetryHandler(RemoteInvokeRetryHandler remoteInvokeRetryHandler) {
        this.remoteInvokeRetryHandler = remoteInvokeRetryHandler;
    }

    public void setHttpClientRequestCustomizer(HttpClientRequestCustomizer httpClientRequestCustomizer) {
        executor.setHttpClientRequestCustomizer(httpClientRequestCustomizer);
    }

}
