package com.xmg.learn.proto.grpc;

import com.xmg.learn.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GrapcClient {
    public static void main(String[] args) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("127.0.0.1", 8888).usePlaintext().build();
//        StudentServiceGrpc.StudentServiceBlockingStub blockingStub = StudentServiceGrpc.newBlockingStub(managedChannel);
//        MyResponse response = blockingStub.getRealnameByUsername(MyRequest.newBuilder().setUsername("张三").build());
//        System.out.println(response.getRealname());
//
//
//        Iterator<StudentResponse> studentResp = blockingStub.getStudentByAge(StudentRequest.newBuilder().setAge(16)
//                .build());
//
//        while (studentResp.hasNext()) {
//            StudentResponse student = studentResp.next();
//            System.out.println(student.getName() + ","
//                    + student.getAge() + "," + student.getCity());
//            //            System.out.println(student.getAge());
//            //            System.out.println(student.getCity());
//        }

//        StudentServiceGrpc.StudentServiceStub asynStub = StudentServiceGrpc.newStub(managedChannel);
//
//
//        final CountDownLatch finishLatch = new CountDownLatch(1);
//
//        StreamObserver<StudentResponseList> streamObserver = new StreamObserver<StudentResponseList>() {
//            @Override
//            public void onNext(StudentResponseList studentRequest) {
//                studentRequest.getStudentResponseList().forEach(studentResponse -> {
//                    System.out.println(studentResponse.getName());
//                    System.out.println(studentResponse.getAge());
//                    System.out.println(studentResponse.getCity());
//                    System.out.println("************************");
//                });
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                System.out.println(throwable.getMessage());
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("onCompleted.............");
//            }
//        };
//
//        StreamObserver<StudentRequest> studentRequestStreamObserver = asynStub.getStudentWrapperByAges(streamObserver);
//        studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(20).build());
//        studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(30).build());
//        studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(40).build());
//        studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(50).build());
//        studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(60).build());
//        studentRequestStreamObserver.onCompleted();

        StudentServiceGrpc.StudentServiceStub newStub = StudentServiceGrpc.newStub(managedChannel);
        StreamObserver<StreamRequest> streamObserver = newStub.biTalk(new StreamObserver<StreamResponse>
                () {
            @Override
            public void onNext(StreamResponse streamResponse) {
                System.out.println(streamResponse.getResponseInfo());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("onCompleted.......");
            }
        });

        for (int i = 0; i < 10; i++) {
            streamObserver.onNext(StreamRequest.newBuilder().setRequestInfo(LocalDateTime.now().toString()).build());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
