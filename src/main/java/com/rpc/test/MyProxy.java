package com.rpc.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MyProxy implements InvocationHandler {


    public static void main(String[] args) {
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //PredoSomething
        method.invoke(args);
        //PostdoSomething
        return null;
    }
}
