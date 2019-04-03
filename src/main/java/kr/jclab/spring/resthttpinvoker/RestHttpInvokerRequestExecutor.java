package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class RestHttpInvokerRequestExecutor implements HttpInvokerRequestExecutor {
    private final HttpComponentsClientHttpRequestFactory httpRequestFactory = new MyHttpComponentsClientHttpRequestFactory();
    private HttpClientRequestCustomizer httpClientRequestCustomizer = null;
    private ObjectMapper objectMapper = new ObjectMapper()
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

    public void setHttpClient(HttpClient httpClient) {
        httpRequestFactory.setHttpClient(httpClient);
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setConnectTimeout(int connectTimeout) {
        httpRequestFactory.setConnectTimeout(connectTimeout);
    }

    public void setReadTimeout(int readTimeout) {
        httpRequestFactory.setReadTimeout(readTimeout);
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        httpRequestFactory.setConnectionRequestTimeout(connectionRequestTimeout);
    }

    public void setHttpClientRequestCustomizer(HttpClientRequestCustomizer httpClientRequestCustomizer) {
        this.httpClientRequestCustomizer = httpClientRequestCustomizer;
    }

    @Override
    public RemoteInvocationResult executeRequest(HttpInvokerClientConfiguration config, RemoteInvocation invocation) throws Exception {
        return null;
    }

    public RemoteInvocationResult jacksonExecuteRequest(HttpInvokerClientConfiguration config, RemoteInvocation invocation, MethodInvocation originalInvocation) throws IOException {
        ClientHttpRequest clientHttpRequest;
        try {
            clientHttpRequest = httpRequestFactory.createRequest(new URI(config.getServiceUrl()), HttpMethod.POST);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        new StringHttpMessageConverter().write(this.objectMapper.writeValueAsString(invocation), MediaType.APPLICATION_JSON_UTF8, clientHttpRequest);
        ClientHttpResponse clientHttpResponse = clientHttpRequest.execute();
        InputStream responseBody = clientHttpResponse.getBody();
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

    private final class MyHttpComponentsClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {
        @Override
        protected void postProcessHttpRequest(HttpUriRequest request) {
            if(httpClientRequestCustomizer != null) {
                httpClientRequestCustomizer.postProcessHttpRequest(request);
            }
        }
    }
}
