package kr.jclab.spring.resthttpinvoker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestHttpInvokerContext {
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public RestHttpInvokerContext(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpServletRequest getHttpServletRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

}
