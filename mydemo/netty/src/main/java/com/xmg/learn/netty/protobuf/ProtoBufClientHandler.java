package com.xmg.learn.netty.protobuf;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.Random;

public class ProtoBufClientHandler extends SimpleChannelInboundHandler<MyDataInfo.MyMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MyDataInfo.MyMessage msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        MyDataInfo.MyMessage myMessage = null;
        int randomInt = new Random().nextInt(3);
        if (0 == randomInt) {
            myMessage = MyDataInfo.MyMessage.newBuilder().setDataType(MyDataInfo.MyMessage.DataType
                    .PersonType)
                    .setPerson(MyDataInfo.Person.newBuilder().setName("张三").setAge(20).setAddress("广州").build())
                    .build();
        } else if (1 == randomInt) {
            myMessage = MyDataInfo.MyMessage.newBuilder().setDataType(MyDataInfo.MyMessage.DataType
                    .DogType)
                    .setDog(MyDataInfo.Dog.newBuilder().setName("一只狗").setAge(20).build())
                    .build();
        } else {
            myMessage = MyDataInfo.MyMessage.newBuilder().setDataType(MyDataInfo.MyMessage.DataType
                    .CatType)
                    .setCat(MyDataInfo.Cat.newBuilder().setName("一条猫").setType("中华猫").build())
                    .build();
        }
        ctx.channel().writeAndFlush(myMessage);
    }
}
