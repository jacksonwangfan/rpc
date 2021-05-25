package com.rpc.test;


import com.rpc.client.balance.LoadBalance;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ServiceLoderDemo {
    public static void main(String[] args) {
        ServiceLoader<LoadBalance> load = ServiceLoader.load(LoadBalance.class);
        Iterator<LoadBalance> iterator = load.iterator();
        while (iterator.hasNext()) {
            LoadBalance next = iterator.next();
            String value = next.getClass().getAnnotation(com.rpc.annotation.LoadBalance.class).value();
            System.out.println(value);
        }
    }
}
