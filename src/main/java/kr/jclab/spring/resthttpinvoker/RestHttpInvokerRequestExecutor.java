package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.jclab.spring.resthttpinvoker.exception.RemoteException;
import kr.jclab.spring.resthttpinvoker.vo.RemoteExceptionData;
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
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class RestHttpInvokerRequestExecutor implements HttpInvokerRequestExecutor {
    private final HttpComponentsClientHttpRequestFactory httpRequestFactory = new MyHttpComponentsClientHttpRequestFactory();
    private HttpClientRequestCustomizer httpClientRequestCustomizer = null;
    private ObjectMapper objectMapper = new ObjectMapper();

    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

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

    protected void validateResponse(HttpInvokerClientConfiguration config, ClientHttpResponse clientHttpResponse)
            throws IOException {

        if (clientHttpResponse.getRawStatusCode() >= 300) {
            throw new IOException(
                    "Did not receive successful HTTP response: status code = " + clientHttpResponse.getRawStatusCode() +
                            ", status message = [" + clientHttpResponse.getStatusText() + "]");
        }
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

        validateResponse(config, clientHttpResponse);

        InputStream responseBody = clientHttpResponse.getBody();
        if(originalInvocation != null) {
            Map<String, Object> result = this.objectMapper.readValue(responseBody, Map.class);
            List value = (List)result.get("value");
            Object exception = result.get("exception");
            RemoteInvocationResult remoteInvocationResult = new RestHttpRemoteInvocationResult();
            if(value != null) {
                Class returnTypeClazz = null;
                if(value.get(0) != null) {
                    try {
                        returnTypeClazz = this.beanClassLoader.loadClass((String)value.get(0));
                    } catch (ClassNotFoundException e) {
                        returnTypeClazz = originalInvocation.getMethod().getReturnType();
                    }
                }
                remoteInvocationResult.setValue(this.objectMapper.convertValue(value.get(1), returnTypeClazz));
            }
            if(exception != null) {
                RemoteExceptionData remoteExceptionData = this.objectMapper.convertValue(exception, RemoteExceptionData.class);
                remoteInvocationResult.setException(RemoteException.create(remoteExceptionData));
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
