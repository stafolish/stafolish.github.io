package com.xmg.learn.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;

public class ProtoBufTest {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        DataInfo.Student.Builder builder = DataInfo.Student.newBuilder();

        DataInfo.Student student = builder.setName("张三").setAge(20).setAddress("广州").build();

        byte[] bytes = student.toByteArray();

        DataInfo.Student student2 = DataInfo.Student.parseFrom(bytes);

        System.out.println(student2.getName());
        System.out.println(student2.getAge());
        System.out.println(student2.getAddress());
    }
}
