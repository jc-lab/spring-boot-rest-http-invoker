package kr.jclab.spring.springbootresthttpinvoker;

import kr.jclab.spring.resthttpinvoker.exception.RemoteException;

import java.io.IOException;

public class TestServiceImpl implements TestService {
    @Override
    public String test1(int a, int b) {
        return "a=" + a + ", b=" + b;
    }

    @Override
    public String exceptionTest1(int a, int b) {
        throw new RemoteException(new IOException("TEST EXCEPTION a=" + a + ", b=" + b));
    }
}
