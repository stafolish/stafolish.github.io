package com.xmg.learn.proxy;

public class TestProxy {

    public static void main(String[] args) {
        Cal cal = new Caculator();
        CaculatorProxy proxy = new CaculatorProxy();
        Cal calculator = proxy.getLogProxy(cal);
        calculator.add(12,23);
    }
}
