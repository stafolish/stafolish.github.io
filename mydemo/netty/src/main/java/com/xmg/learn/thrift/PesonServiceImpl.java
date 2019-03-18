package com.xmg.learn.thrift;

import com.xmg.learn.thrift.generated.DataException;
import com.xmg.learn.thrift.generated.Person;
import com.xmg.learn.thrift.generated.PersonService;
import org.apache.thrift.TException;

public class PesonServiceImpl implements PersonService.Iface {
    @Override
    public Person getPersonByUsername(String username) throws DataException, TException {

        System.out.println("Got Client Param:" +username);
        Person person = new Person();
        person.setAge(20);
        person.setUsername("张三");
        person.setMarried(false);

        return person;
    }

    @Override
    public void savePerson(Person person) throws DataException, TException {
        System.out.println("get Client Param:");

        System.out.println(person.getUsername());
        System.out.println(person.getAge());
        System.out.println(person.isMarried());
    }
}
