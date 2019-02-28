package kr.jclab.spring.resthttpinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

public class RestHttpInvokerProxyFactoryBean extends HttpInvokerProxyFactoryBean {
    private ObjectMapper objectMapper = new ObjectMapper();
    private RestHttpInvokerRequestExecutor executor = new RestHttpInvokerRequestExecutor();

    public RestHttpInvokerProxyFactoryBean() {
        super();
        setHttpInvokerRequestExecutor(executor);
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        executor.setObjectMapper(objectMapper);
    }
}
