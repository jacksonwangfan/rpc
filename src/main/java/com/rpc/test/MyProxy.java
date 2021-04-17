package com.rpc.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class Cat implements Animal{

    @Override
    public String run() {
        System.out.println("Cat run");
        return "Cat run";
    }
}

public class MyProxy implements InvocationHandler {

    private Object target;

   public MyProxy(Object target){
       this.target = target;
   }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("preDoSomething");
        Object res = method.invoke(target,args);
        System.out.println("postDoSomething"+res);
        return res;
    }

    public Object getInstence(){
      return Proxy.newProxyInstance(target.getClass().getClassLoader(),target.getClass().getInterfaces(),this);
    }

    public static void main(String[] args) {
        MyProxy myProxy = new MyProxy(new Cat());
        Animal animal = (Animal) myProxy.getInstence();
        animal.run();

    }

}
