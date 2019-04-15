---
layout: post
title:  RabbitMQ与Spring整合
date:   2019-04-13 13:18:00 +0800
categories: 中间件
tag: RabbitMQ
---



## RabbitMQ与Spring整合

### RabbitMQ整合SpringAMQP

#### RabbitAdmin

注意：autoStart必须设置为true，否则Spring容器不会加载RabbitAdmin

RabbitAdmin底层实现就是从Spring容器中获取Exchange、Binding、RoutingKey以及Queue的@Bean声明

然后RabbitTemplate可以使用execute方法执行对应的声明、修改、删除等一系列RabbitMQ基础功能操作



#### 配置类

```java
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.xmg.rabbitmq")
public class RabbitMQConfig {

    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses("192.168.0.132:5672");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");
        return connectionFactory;
    }


    @Bean
    public RabbitAdmin rabbitAdmin(){
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }
}
```

#### 测试用例

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringRabbitmqApplicationTests {


    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Test
    public void test1() {

        rabbitAdmin.declareExchange(new DirectExchange("test.direct", false, false));
        rabbitAdmin.declareExchange(new TopicExchange("test.topic", false, false));
        rabbitAdmin.declareExchange(new FanoutExchange("test.fanout", false, false));


        rabbitAdmin.declareQueue(new Queue("test.direct.queue", false));
        rabbitAdmin.declareQueue(new Queue("test.topic.queue", false));
        rabbitAdmin.declareQueue(new Queue("test.fanout.queue", false));

        rabbitAdmin.declareBinding(new Binding("test.direct.queue", Binding.DestinationType.QUEUE,
                "test.direct", "direct", new HashMap<>()));

        rabbitAdmin.declareBinding(BindingBuilder
                .bind(new Queue("test.topic.queue", false))
                .to(new TopicExchange("test.topic", false, false))
                .with("user.#"));

        rabbitAdmin.declareBinding(BindingBuilder
                .bind(new Queue("test.fanout.queue", false))
                .to(new FanoutExchange("test.fanout", false, false))
        );

        rabbitAdmin.purgeQueue("test.topic.queue", false);


    }

}
```



#### SpringAMQP声明

```java
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.xmg.rabbitmq")
public class RabbitMQConfig {

    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses("192.168.0.132:5672");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");
        return connectionFactory;
    }


    @Bean
    public RabbitAdmin rabbitAdmin(){
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public TopicExchange exchange001() {
        return new TopicExchange("topic001");
    }

    @Bean
    public Queue queue001(){
        return new Queue("queue001");
    }

    @Bean
    public Binding binding001(){
        return BindingBuilder.bind(queue001()).to(exchange001()).with("spring.*");
    }

    @Bean
    public TopicExchange exchange002(){
        return new TopicExchange("topic002");
    }

    @Bean
    public Queue queue002(){
        return new Queue("queue002");
    }

    @Bean
    public Binding binding002(){
        return BindingBuilder.bind(queue002()).to(exchange002()).with("rabbit.*");
    }



    @Bean
    public Queue queue003(){
        return new Queue("queue003");
    }

    @Bean
    public Binding binding003(){
        return BindingBuilder.bind(queue003()).to(exchange001()).with("mq.*");
    }

    @Bean
    public Queue queue_image(){
        return new Queue("image_queue");
    }

    @Bean
    public Queue queue_pdf(){
        return new Queue("pdf_queue");
    }


}
```



#### RabbitTemplate

消息模板

在与SpringAMQP整合的时候进行发送消息的关键类

该类提供了丰富的发送消息方法，包括可靠性投递消息方法、回调监听消息接口ConfirmCallback、返回值确认ReturnCallback等等。同样需要注入到spring容器当中，然后直接使用。



测试用例

```java

@Autowired
private RabbitTemplate rabbitTemplate;
@Test
public void testTemplate(){
    MessageProperties properties = new MessageProperties();
    properties.getHeaders().put("desc","信息描述。。。。");
    properties.getHeaders().put("type","自定义消息。。。。");

    Message message = new Message("hello rabbitmq".getBytes(),properties);

    rabbitTemplate.convertAndSend("topic001", "spring.amqp", message, new MessagePostProcessor() {
        @Override
        public Message postProcessMessage(Message message) throws AmqpException {

            System.out.println("-------添加额外设置-------");
            message.getMessageProperties().getHeaders().put("desc","额外修改的信息描述");
            message.getMessageProperties().getHeaders().put("attr","额外新加的属性");
            return message;
        }
    });
}

@Test
public void testTemplate2(){
    MessageProperties properties = new MessageProperties();
    properties.setContentType("text/plain");

    Message message = new Message("mq 消息1234".getBytes(),properties);

    rabbitTemplate.send("topic001", "spring.abc", message);
    rabbitTemplate.convertAndSend("topic001", "spring.amqp", "hello object message send");
    rabbitTemplate.convertAndSend("topic002", "rabbit.abc", "hello object message send");
}
```

#### SimpleMessageListenerContainer

很强大的类，可以对他进行很多设置，对于消费者的配置项，这个类都可以满足

- 监听队列（多个队列），自动启动，自动声明功能

- 设置事务特性，事务管理器，事务属性，事务容量（并发），是否开启事务，回滚消息等（了解即可）
- 设置消费者数量，最小最大数量，批量消费
- 消息的确认和自动确认模式，是否重回队列，异常捕获handler函数。
- 设置消费者标签生成策略，是否独占模式，消费者属性
- 设置具体的监听器，消息转换器等等
- SimpleMessageListenerContainer可以进行动态设置，比如在运行的应用可以动态的修改器消费者数量的大小，接收消息的模式等
- 很多基于RabbitMQ的自动定制化后端管控台在进行动态设置的时候，也是根据这一特性去实现的。所以可以看出SpringAMQP非常的强大

#### 问题：SimpleMessageListenerContainer为什么能够动态的感知配置的变更？

config类中添加bean

```java
@Bean
public SimpleMessageListenerContainer messageListenerContainer() {
    SimpleMessageListenerContainer container =
            new SimpleMessageListenerContainer(connectionFactory());
    container.setQueues(queue001(), queue002(), queue003(), queue_image(), queue_pdf());
    container.setConcurrentConsumers(1);
    container.setMaxConcurrentConsumers(5);
    container.setDefaultRequeueRejected(false);
    container.setAcknowledgeMode(AcknowledgeMode.AUTO);
    container.setExposeListenerChannel(true);
    
    //自定义tag生成策略
    container.setConsumerTagStrategy(queue -> queue + "_" + UUID.randomUUID().toString());

    ChannelAwareMessageListener listener = (Message message, Channel channel) -> {
        String msg = new String(message.getBody());
        System.out.println("----------------消费者：" + msg);
    };

    container.setMessageListener(listener);

    return container;
}
```





#### MessageListenerAdapter

1. 适配器方式：
   1. 默认方法模式，自定义消息处理类添加方法：handleMessage
   2. 指定方法名字：consumeMessage
   3. 添加一个转换器：TextMessageConverter

```java
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer() {
        SimpleMessageListenerContainer container =
                new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(queue001(), queue002(), queue003(), queue_image(), queue_pdf());
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(5);
        container.setDefaultRequeueRejected(false);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setExposeListenerChannel(true);

        //自定义tag生成策略
        container.setConsumerTagStrategy(queue -> queue + "_" + UUID.randomUUID().toString());
        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());
        adapter.setDefaultListenerMethod("consumeMessage");
        adapter.setMessageConverter(new TextMessageConverter());
        container.setMessageListener(adapter);

        return container;
    }
```



```java
public class MessageDelegate {

    public void handleMessage(byte[] messageBody){
        System.out.println("默认方法，消息内容："+new String(messageBody));
    }
    
    public void consumeMessage(byte[] messageBody) {
        System.err.println("字节数组方法，消息内容:" + new String(messageBody));
    }

    public void consumeMessage(String messageBody) {
        System.err.println("字符串方法，消息内容:" + messageBody);
    }
}
```



```java
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

public class TextMessageConverter implements MessageConverter {
    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        return new Message(object.toString().getBytes(), messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        String contentType = message.getMessageProperties().getContentType();
        if (null != contentType && contentType.contains("text")) {
            return new String(message.getBody());
        }
        return message.getBody();
    }
}
```

2. 适配器方式：

   使用queueOrTagToMethodName添加消息队列和方法的映射



```java
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer() {
        SimpleMessageListenerContainer container =
                new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(queue001(), queue002(), queue003(), queue_image(), queue_pdf());
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(5);
        container.setDefaultRequeueRejected(false);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setExposeListenerChannel(true);

        //自定义tag生成策略
        container.setConsumerTagStrategy(queue -> queue + "_" + UUID.randomUUID().toString());
        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());

        adapter.setMessageConverter(new TextMessageConverter());
        Map<String, String> queueOrTagToMethodName = new HashMap<>();
        queueOrTagToMethodName.put("queue001","method1");
        queueOrTagToMethodName.put("queue002","method2");
        adapter.setQueueOrTagToMethodName(queueOrTagToMethodName);
        container.setMessageListener(adapter);

        return container;
    }
```



MessageListenerAdapter核心属性：

- defaultListenerMethod默认监听方法名称：用于设置默认监听方法名称
- Delegate委托对象：实际真实的委托对象，用于处理消息
- queueOrTagToMethodName队列标识与方法名称组成的集合，可以一一进行队列与方法名称的匹配，队列和方法名称绑定，即指定队列里的消息会被绑定的方法所接受处理



#### MessageConverter

消息转换器：

在进行发送消息的时候，正常情况下消息体为二进制的数据方式进行传输，如果希望内部帮我们进行转换，或者指定自定义的转换器，就需要用到MessageConverter

1. 实现MessageConverter接口

2. 重写两个方法：

   toMessage：java对象转换为Message

   fromMessage：Message对象转换为java对象

Json转换器：Jackson2JsonMessageConverter：可以进行java对象转换

DefaultJackson2JavaTypeMapper映射器：可以进行java对象的映射关系

自定义二进制转换器：比如图片类型、PDF、PPT、流媒体



### RabbitMQ整合Spring Boot



publisher-confirms,实现一个监听器用于监听Broker端给我们返回的确认请求：

RabbitTemplate.confirmCallback



publisher-returns,保证消息对Broker端是可达的，如果出现路由键不可达的情况，则使用监听器对不可达的消息进行后续的处理，保证消息的路由成功：

RabbitTemplate.returnCallback

在发送消息的时候对template进行配置mandatory=true保证监听有效



生产端还可以配置其他属性，比如发送重试，超时时间，次数，间隔等



消费端核心配置

```yml
spring:
  rabbitmq:
    listener:
      simple:
        concurrency: 1
        max-concurrency: 5
        acknowledge-mode: manual
        
```

- 首先配置手工签收模式，用于ACK的手工处理，这样我们可以保证消息的可靠性送达，或者在消费端消费失败的时候可以做到重回队列，根据业务记录日志等处理

- 可以设置消费端的监听个数和最大个数，用于控制消费端的并发情况

- 消费端监听@RabbitMQListener注解，这个对于在实际工作中非常的好用

- @RabbitListener是一个组合注解，里面可以注解配置

  @QueueBinding，@Queue，@Exchange直接通过这个组合注解一次性搞定消费端交换机，队列，绑定，路由，并且配置监听功能







### RabbitMQ整合Spring Cloud

Barista接口：Barista接口是定义来作为后面类的参数，这一接口定义通道类型和通道名称，通道民初时作为配置用，通道类型则决定了app会使用这一通道进行发送消息还是从中接收消息

@Output：输出注解，用于定义发送消息接口

@Input：输入注解，用于定义消息的发送接口

@StreamListener：用于定义监听方法的注解



SpringCloudStream框架有一个非常大的问题就是不能实现可靠性的投递，也就是没法保证消息的100%可靠性，会存在少量消息丢失的问题

这个原因时因为SpringCloudStream框架为了和Kafka兼顾，所以在实际工作中使用它的目的就是针对高性能的消息通信的。这个就是SpringCloudStream的定位