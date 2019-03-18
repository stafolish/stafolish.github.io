package com.xmg.learn.proto.grpc;

import com.xmg.learn.proto.*;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class StudentServiceImp extends StudentServiceGrpc.StudentServiceImplBase {

    @Override
    public void getRealnameByUsername(MyRequest request, StreamObserver<MyResponse> responseObserver) {
        System.out.println("接受到客户的信息：" + request.getUsername());
        responseObserver.onNext(MyResponse.newBuilder().setRealname("lisi").build());
        responseObserver.onCompleted();
    }

    @Override
    public void getStudentByAge(StudentRequest request, StreamObserver<StudentResponse> responseObserver) {
        System.out.println("接受到客户的信息：" + request.getAge());
        responseObserver.onNext(StudentResponse.newBuilder().setName("哈哈").setAge(16).setCity("广州").build());
        responseObserver.onNext(StudentResponse.newBuilder().setName("流弊").setAge(26).setCity("上海").build());
        responseObserver.onNext(StudentResponse.newBuilder().setName("卧槽").setAge(18).setCity("深圳").build());

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<StudentRequest> getStudentWrapperByAges(StreamObserver<StudentResponseList>
                                                                              responseObserver) {
        return new StreamObserver<StudentRequest>() {
            @Override
            public void onNext(StudentRequest studentRequest) {
                System.out.println("onNext:" + studentRequest.getAge());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                StudentResponse studentResponse1 = StudentResponse.newBuilder().setName("张三").setAge(20).setCity("西安").build();
                StudentResponse studentResponse2 = StudentResponse.newBuilder().setName("李四").setAge(30).setCity("广州").build();


                StudentResponseList studentResponseList = StudentResponseList.newBuilder().addStudentResponse(studentResponse1)
                        .addStudentResponse(studentResponse2).build();

                responseObserver.onNext(studentResponseList);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<StreamRequest> biTalk(StreamObserver<StreamResponse> responseObserver) {
        return new StreamObserver<StreamRequest>() {
            @Override
            public void onNext(StreamRequest streamRequest) {
                System.out.println("streamRequest"+streamRequest.getRequestInfo());
                responseObserver.onNext(StreamResponse.newBuilder().setResponseInfo(UUID.randomUUID().toString()).build());

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
