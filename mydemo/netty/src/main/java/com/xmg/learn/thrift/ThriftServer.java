package com.xmg.learn.thrift;

import com.xmg.learn.thrift.generated.PersonService;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;

public class ThriftServer {

    public static final void main(String[] args) throws Exception {
        TNonblockingServerSocket socket = new TNonblockingServerSocket(8888);

        THsHaServer.Args arg = new THsHaServer.Args(socket).minWorkerThreads(2).maxWorkerThreads(4);
        PersonService.Processor<PesonServiceImpl> processor = new PersonService.Processor<>(new PesonServiceImpl());

        //压缩协议
        arg.protocolFactory(new TCompactProtocol.Factory());
        arg.transportFactory(new TFastFramedTransport.Factory());
        arg.processorFactory(new TProcessorFactory(processor));

        //THsHaServer半同步半异步
        TServer server = new THsHaServer(arg);

        System.out.println("Thrift Server Started");

        server.serve();
    }
}
