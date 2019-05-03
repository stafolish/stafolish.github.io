---
layout: post
title:  RabbitMQ集群架构KeepAlived
date:   2019-04-16 23:20:00 +0800
categories: 中间件
tag: RabbitMQ
---



## RabbitMQ集群架构KeepAlived



KeepAlived软件主要通过VRRP协议实现高可用功能的。VRRP是Virtual Router Redundancy Protocol（虚拟路由器冗余协议）的缩写，VRRP出现的目的就是为了解决静态路由单独故障问题的，他能够保证当个别节点当机时，整个网络可以不间断地运行，所以，KeepAlived一方面具有配置管理LVS的功能，同时还具有对LVS下面节点进行健康检查的功能，另一方面也可以实现系统网络服务的高可用功能。



KeepAlived功能：

1. 管理LVS负载均衡软件
2. 实现LVS集群节点的健康检查
3. 作为系统网络的高可用性（failover）





KeepAlived高可用原理：

KeepAlived高可用服务对之间的故障切换转移，是通过VRRP来实现的。在KeepAlived服务正常工作时，主Master节点会不断地向备节点发送（多播的方式）心跳消息，用以告诉备节点（Backup）节点自己还活着，当主Master节点发送故障时，就无法发送心跳消息，备节点就因此无法继续检测来自Master节点的心跳了，于是调用自身接管程序，接管主Master节点的IP资源及服务。而当主Master节点恢复时，备节点又会释放主节点故障时自身接管的IP资源及服务，恢复到原来的备用角色



```shell
yum install -y openssl openssl-devel
```





```java
cp /usr/local/keepalived-2.0.15/etc/keepalived/keepalived.conf /etc/keepalived/

cp /usr/local/keepalived-2.0.15/etc/rc.d/init.d/keepalived /etc/init.d/

cp /usr/local/keepalived-2.0.15/etc/sysconfig/keepalived /etc/sysconfig/
    
    
ln -s /usr/local/sbin/keepalived /usr/sbin/
ln -s /usr/local/keepalived/sbin/keepalived /sbin/
//可以设置开机启动：chkconfig keepalived on，到此我们安装完毕!
chkconfig keepalived on

```

