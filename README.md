# spring-boot-rest-http-invoker



기존의 HttpInvoker는 객체를 Serialize할 때 JavaSerializer을 사용합니다.

고로 JAVA 9 이전에서는 서로 다른 버전의 JVM이거나 사소한 객체의 버전이 다른 경우 정상적으로 동작하지 않습니다.

**spring-boot-rest-http-invoker**는 JavaSerializer 대신에 Jackson ObjectMapper를 사용하여 Restful API로 변환합니다.

RemoteInvocation 형식을 맞춘다면 PHP나 다른 언어에서도 동일하게 접근할 수 있습니다.

프로토콜 형식에 대해서는 아래를 참고해 주세요.



github : https://github.com/jc-lab/spring-boot-rest-http-invoker

bintray : https://bintray.com/jc-lab/spring.boot/spring-boot-rest-http-invoker



Maven

```xml
<dependency>
  <groupId>kr.jclab.spring</groupId>
  <artifactId>spring-boot-rest-http-invoker</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

Gradle

```groovy
implementation 'kr.jclab.spring:spring-boot-rest-http-invoker:1.0.0'
```





## 예제 소스

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeronsoftn.demo.demo1rpc.controller.TestContoller;
import kr.jclab.spring.resthttpinvoker.RestHttpInvokerProxyFactoryBean;
import kr.jclab.spring.resthttpinvoker.RestHttpInvokerServiceExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

@Configuration
public class RPCConfig {
    // Invoker 서버에서
    @Bean("/api/test")
    HttpInvokerServiceExporter apiExporter(TestService testService) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(TestContoller.Test.class);
        RestHttpInvokerServiceExporter exporter = new RestHttpInvokerServiceExporter();
        exporter.setObjectMapper(objectMapper);
        exporter.setService(testService);
        exporter.setServiceInterface(TestService.class);
        return exporter;
    }
	
    // Invoker Client에서
    @Bean
    HttpInvokerProxyFactoryBean testService() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(TestContoller.Test.class);
        RestHttpInvokerProxyFactoryBean factoryBean = new RestHttpInvokerProxyFactoryBean();
        factoryBean.setObjectMapper(objectMapper);
        factoryBean.setServiceUrl("http://127.0.0.1:8080/api/test");
        factoryBean.setServiceInterface(TestService.class);
        return factoryBean;
    }

}

```





### 예제 Interface

```java
public interface TestService {
    void test_1();
    void test_2(int a);
    void test_3(String b);
    void test_4(Map<String, String> c);
    int test_5(int a, int b);
}
```

---

#### `void test1()` 실행시

```http
POST /api/test HTTP/1.1
Content-Type: application/json
Accept-Language: ko-KR
Accept-Encoding: gzip
User-Agent: Java/1.8.0_191
Host: 127.0.0.1:9999
Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
Connection: keep-alive
Content-Length: 76

{"methodName":"test_1","parameterTypes":[],"arguments":[],"attributes":null}
```

```http
HTTP/1.1 200 
Content-Type: application/json
Content-Length: 31
Date: Thu, 28 Feb 2019 01:27:24 GMT

{"value":null,"exception":null}
```

---

### `void test_2(10)` 실행시

```http
POST /api/test HTTP/1.1
Content-Type: application/json
Accept-Language: ko-KR
Accept-Encoding: gzip
User-Agent: Java/1.8.0_191
Host: 127.0.0.1:9999
Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
Connection: keep-alive
Content-Length: 83

{"methodName":"test_2","parameterTypes":["int"],"arguments":[10],"attributes":null}
```

```http
HTTP/1.1 200 
Content-Type: application/json
Content-Length: 31
Date: Thu, 28 Feb 2019 01:27:24 GMT

{"value":null,"exception":null}
```

---

### `void test_3("20")` 실행시

```http
POST /api/test HTTP/1.1
Content-Type: application/json
Accept-Language: ko-KR
Accept-Encoding: gzip
User-Agent: Java/1.8.0_191
Host: 127.0.0.1:9999
Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
Connection: keep-alive
Content-Length: 98

{"methodName":"test_3","parameterTypes":["java.lang.String"],"arguments":["20"],"attributes":null}
```

```http
HTTP/1.1 200 
Content-Type: application/json
Content-Length: 31
Date: Thu, 28 Feb 2019 01:27:24 GMT

{"value":null,"exception":null}
```

---

### `void test_4(Map<String, Object> map)` 실행시

```java
public static class Test {
        public int a;
        public int b;

        public Test() {}

        public Test(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    @RequestMapping(path = "/test-1")
    @ResponseBody
    public String test1() {
        HashMap<String, Object> test = new HashMap<>();
        test.put("a", "aaaa");
        test.put("b", 123123);
        test.put("c", 3.14);
        test.put("d", new Test(10, 20));
        apiProxy.test_4(test);
        return "OK : " + apiProxy.test_5(22, 55);
    }
```

```http
POST /api/test HTTP/1.1
Content-Type: application/json
Accept-Language: ko-KR
Accept-Encoding: gzip
User-Agent: Java/1.8.0_191
Host: 127.0.0.1:9999
Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
Connection: keep-alive
Content-Length: 143

{"methodName":"test_4","parameterTypes":["java.util.Map"],"arguments":[{"a":"aaaa","b":123123,"c":3.14,"d":{"a":10,"b":20}}],"attributes":null}
```

```java
HTTP/1.1 200 
Content-Type: application/json
Content-Length: 31
Date: Thu, 28 Feb 2019 01:47:29 GMT

{"value":null,"exception":null}
```

---

### `int test_5(22, 55)` 실행시

```http
POST /api/test HTTP/1.1
Content-Type: application/json
Accept-Language: ko-KR
Accept-Encoding: gzip
User-Agent: Java/1.8.0_191
Host: 127.0.0.1:9999
Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
Connection: keep-alive
Content-Length: 92

{"methodName":"test_5","parameterTypes":["int","int"],"arguments":[22,55],"attributes":null}
```

```http
HTTP/1.1 200 
Content-Type: application/json
Content-Length: 29
Date: Thu, 28 Feb 2019 01:47:29 GMT

{"value":77,"exception":null}
```

