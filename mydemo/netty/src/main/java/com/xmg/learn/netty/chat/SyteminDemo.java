package com.xmg.learn.netty.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SyteminDemo {
    private static int i;
    public static void incr(){
        i++;
    }
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String in = null;
        while ((in=reader.readLine()).length()>0){
            System.out.println(in);
        }

    }
}
