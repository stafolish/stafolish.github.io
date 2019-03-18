

#### NioEventLoopGroup源码分析

在此之前首先要明白：

1. netty是一个nio的框架。

2. 它是为了解决NIO编程的复杂性而存在的。

netty中NioEventLoop,ByteBuf,Channel分别对应着NIO编程的三个核心Selector,ByteBuffer,Channel,只不过netty对NIO的三个组件都有一定程度的封装和优化。

   

下面代码是一个经典的netty服务端代码，第一行代码就是创建一个NioEventLoopGroup对象，这个bossGroup的主要作用就是接受来自客户端的连接，然后将连接转给第二个EventLoopGroup（即workGroup）进行处理。	

```
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MyChildHandler());
            ChannelFuture future = bootstrap.bind(8899).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {

        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
```

##### 	netty为何在服务启动辅助类（ServerBootstrap）中要设置两个EventLoopGroup呢？

实际上是因为bossGroup和workGroup分别对应着操作系统中存放tcp三次握手未完成（**syns queue半连接队列**）和已经建立连接(**accept queue 全连接队列**)的两个队列。

![]({{ '/styles/images/netty\boss_work_group.PNG' | prepend: site.baseurl }})

在连接建立好之后，bossGroup 不对连接进行任何处理，而是将连接交给workGroup处理，而用户业务逻辑的真正处理是在workGroup中完成的。

EventLoopGroup中的新方法如下，实际上用到的只有三个（有一个被标记为过时的方法）。而且第一个方法父接口中也有，覆盖有什么含义吗？好像并没有什么用。

```java
public interface EventLoopGroup extends EventExecutorGroup {
    /**
     * Return the next EventLoop to use 都是接口方法，覆盖有什么意义吗？
     */
    @Override
    EventLoop next();

    /**
     * Register a Channel with this EventLoop. The returned ChannelFuture
     * will get notified once the registration was complete.
     */
    ChannelFuture register(Channel channel);

    /**
     * Register a Channel with this EventLoop using a ChannelFuture. The passed
     * ChannelFuture will get notified once the registration was complete and also will get returned.
     */
    ChannelFuture register(ChannelPromise promise);

    /**
     * Register a Channel with this EventLoop. The passed ChannelFuture
     * will get notified once the registration was complete and also will get returned.
     *
     * @deprecated Use #register(ChannelPromise) instead.此方法不建议使用，因为promise中已经包含了一个channel
     */
    @Deprecated
    ChannelFuture register(Channel channel, ChannelPromise promise);
}
```

ChannelFuture register(Channel channel)方法是把传入的一个channel绑定到这个EventLoop上，事实上,一个NioEventLoop绑定了一个Selector，如下源码可以证明

```java
public final class NioEventLoop extends SingleThreadEventLoop {

	//...............省略N多代码............
    /**
     * The NIO {@link Selector}.
     */
    private Selector selector;
    private Selector unwrappedSelector;
    private SelectedSelectionKeySet selectedKeys;

    private final SelectorProvider provider;
```

EventLoop是EventLoopGroup的子类，而EventLoopGroup中有对EventLoop的依赖，这个继承和依赖的关系真的有点迷。

```java
public interface EventLoop extends OrderedEventExecutor, EventLoopGroup {
    @Override
    EventLoopGroup parent();
}
```

​	

