package kr.jclab.spring.springbootresthttpinvoker;

import kr.jclab.spring.resthttpinvoker.RestHttpInvokerServiceExporter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestServiceConfig {
    @Bean("/test-api")
    public RestHttpInvokerServiceExporter testApi() {
        RestHttpInvokerServiceExporter serviceExporter = new RestHttpInvokerServiceExporter();
        serviceExporter.setServiceInterface(TestService.class);
        serviceExporter.setService(new TestServiceImpl());
        serviceExporter.afterPropertiesSet();
        return serviceExporter;
    }
}
