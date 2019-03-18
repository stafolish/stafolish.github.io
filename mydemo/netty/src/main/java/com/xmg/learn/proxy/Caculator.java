package com.xmg.learn.proxy;

public class Caculator implements Cal{

    public int add(int x, int y){
        System.out.println("add x+y");
        return x+y;
    }
}
