package com.xmg.learn.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class CaculatorProxy {

    public  <T>T getLogProxy(T target){
        return (T)Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println("-------before invoke--------");
                        Object result = method.invoke(target,args);
                        System.out.println("-------after invoke--------");
                        return result;
                    }
                });
    }
}
