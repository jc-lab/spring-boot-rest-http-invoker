package kr.jclab.spring.resthttpinvoker;

import org.apache.http.client.methods.HttpUriRequest;

public interface HttpClientRequestCustomizer {
    void postProcessHttpRequest(HttpUriRequest request);
}
