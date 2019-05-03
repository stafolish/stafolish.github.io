---
layout: post
title:  RabbitMQ集群架构haproxy
date:   2019-04-15 13:20:00 +0800
categories: 中间件
tag: RabbitMQ
---



## RabbitMQ集群架构haproxy



haproxy.cfg

```
#logging options
global
        log 127.0.0.1 local0 info
        maxconn 5120
        chroot /usr/local/haproxy
        uid 99
        gid 99
        daemon
        quiet
        nbproc 20
        pidfile /var/run/haproxy.pid

defaults
        log global
        #使用4层代理模式，”mode http”为7层代理模式
        mode tcp
        #if you set mode to tcp,then you nust change tcplog into httplog
        option tcplog
        option dontlognull
        retries 3
        option redispatch
        maxconn 2000
        contimeout 5s
     ##客户端空闲超时时间为 60秒 则HA 发起重连机制
     clitimeout 60s
     ##服务器端链接超时时间为 15秒 则HA 发起重连机制
     srvtimeout 15s
#front-end IP for consumers and producters

listen rabbitmq_cluster
        bind 0.0.0.0:5672
        #配置TCP模式
        mode tcp
        #logging options
global
        log 127.0.0.1 local0 info
        maxconn 5120
        chroot /usr/local/haproxy
        uid 99
        gid 99
        daemon
        quiet
        nbproc 20
        pidfile /var/run/haproxy.pid

defaults
        log global
        #使用4层代理模式，”mode http”为7层代理模式
        mode tcp
        #if you set mode to tcp,then you nust change tcplog into httplog
        option tcplog
        option dontlognull
        retries 3
        option redispatch
        maxconn 2000
        contimeout 5s
     ##客户端空闲超时时间为 60秒 则HA 发起重连机制
     clitimeout 60s
     ##服务器端链接超时时间为 15秒 则HA 发起重连机制
     srvtimeout 15s
#front-end IP for consumers and producters

listen rabbitmq_cluster
        bind 0.0.0.0:5672
        #配置TCP模式
        mode tcp
        #balance url_param userid
        #balance url_param session_id check_post 64
        #balance hdr(User-Agent)
        #balance hdr(host)
        #balance hdr(Host) use_domain_only
        #balance rdp-cookie
        #balance leastconn
        #balance source //ip
        #简单的轮询
        balance roundrobin
        #rabbitmq集群节点配置 #inter 每隔五秒对mq集群做健康检查， 2次正确证明服务器可用，2次失败证明服务器不可用，并且配置主备机制
        server xmg140 192.168.0.140:5672 check inter 5000 rise 2 fall 2
        server xmg141 192.168.0.141:5672 check inter 5000 rise 2 fall 2
        server xmg142 192.168.0.142:5672 check inter 5000 rise 2 fall 2
        server xmg143 192.168.0.143:5672 check inter 5000 rise 2 fall 2
#配置haproxy web监控，查看统计信息
listen stats
        bind 192.168.0.145:8100
        mode http
        option httplog
        stats enable
        #设置haproxy监控地址为http://localhost:8100/rabbitmq-stats
        stats uri /rabbitmq-stats
        stats refresh 5s

```



/usr/local/haproxy/sbin/haproxy -f /etc/haproxy/haproxy.cfg

http://192.168.0.144:8100/rabbitmq-stats



![]({{ '/styles/images/mq/haproxy.png' | prepend: site.baseurl }})