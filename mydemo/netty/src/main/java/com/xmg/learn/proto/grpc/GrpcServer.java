package com.xmg.learn.proto.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {

    private Server server;
    private void start() throws IOException {
        this.server = ServerBuilder.forPort(8888)
                .addService(new StudentServiceImp()).build().start();
        System.out.println("Server started");

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("关闭jvm");
            GrpcServer.this.stop();
        }));
    }

    private void stop(){
        if (null != this.server){
            this.server.shutdown();
        }
    }

    private void awaitTermination() throws InterruptedException {
        if (null != this.server){
            //this.server.awaitTermination(3000,TimeUnit.MILLISECONDS);
            this.server.awaitTermination();
        }
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        GrpcServer server = new GrpcServer();
        server.start();
        server.awaitTermination();
    }
}
