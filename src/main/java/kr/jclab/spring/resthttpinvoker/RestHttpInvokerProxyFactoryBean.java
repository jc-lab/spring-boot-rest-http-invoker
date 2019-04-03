package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.http.client.HttpClient;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

public class RestHttpInvokerProxyFactoryBean extends HttpInvokerProxyFactoryBean {
    private ObjectMapper objectMapper = new ObjectMapper();
    private RestHttpInvokerRequestExecutor executor = new RestHttpInvokerRequestExecutor();

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
    protected RemoteInvocationResult executeRequest(RemoteInvocation invocation, MethodInvocation originalInvocation) throws Exception {
        return executor.jacksonExecuteRequest(this, invocation, originalInvocation);
    }

    @Override
    protected RemoteInvocationResult executeRequest(RemoteInvocation invocation) throws Exception {
        return this.executeRequest(invocation, null);
    }

    public void setHttpClientRequestCustomizer(HttpClientRequestCustomizer httpClientRequestCustomizer) {
        executor.setHttpClientRequestCustomizer(httpClientRequestCustomizer);
    }

}
