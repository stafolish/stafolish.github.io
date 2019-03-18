package com.xmg.learn.thrift;

import com.xmg.learn.thrift.generated.Person;
import com.xmg.learn.thrift.generated.PersonService;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class ThriftClient {

    public static void main(String[] args) {
        TTransport tTransport = new TFastFramedTransport(new TSocket("127.0.0.1",8888),600);
        TCompactProtocol protocol = new TCompactProtocol(tTransport);
        PersonService.Client client = new PersonService.Client(protocol);

        try {
            tTransport.open();
            Person person = client.getPersonByUsername("张三");
            System.out.println(person.getUsername());
            System.out.println(person.getAge());
            System.out.println(person.isMarried());

            System.out.println("---------------------------");
            Person person1 = new Person();

            person1.setMarried(true);
            person1.setAge(35);
            person1.setUsername("李四");

            client.savePerson(person1);


        } catch (Exception e) {
        } finally {
            tTransport.close();
        }

    }
}
