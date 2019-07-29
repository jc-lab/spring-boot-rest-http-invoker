package kr.jclab.spring.springbootresthttpinvoker;

import kr.jclab.spring.resthttpinvoker.RestHttpInvokerProxyFactoryBean;
import kr.jclab.spring.resthttpinvoker.exception.RemoteException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {TestApp.class, TestServiceConfig.class}
)
public class SpringBootRestHttpInvokerApplicationTests {

    @LocalServerPort
    private int port;

    @Test
    public void contextLoads() {
    }

    @Test
    public void shouldPassTestExecute1() {
        RestHttpInvokerProxyFactoryBean proxyFactoryBean = new RestHttpInvokerProxyFactoryBean();
        proxyFactoryBean.setServiceInterface(TestService.class);
        proxyFactoryBean.setServiceUrl("http://localhost:" + port + "/test-api");
        proxyFactoryBean.afterPropertiesSet();
        TestService testService = (TestService)proxyFactoryBean.getObject();

        System.out.println("testService.test1(10, 20): " + testService.test1(10, 20));
        try {
            System.out.println("testService.exceptionTest1(10, 20): " + testService.exceptionTest1(10, 20));
        } catch (RemoteException e) {
            System.err.println("==================================================");
            e.printStackTrace();
            System.err.println("==================================================");
        }
    }

}
