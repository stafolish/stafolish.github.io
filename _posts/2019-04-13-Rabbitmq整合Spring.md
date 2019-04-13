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





#### SpringAMQP声明

#### RabbitTemplate

#### SimpleMessageListenerContainer

#### MessageListenerAdapter

#### MessageConverter

序列化反序列化操作的类







### RabbitMQ整合Spring Boot











### RabbitMQ整合Spring Cloud