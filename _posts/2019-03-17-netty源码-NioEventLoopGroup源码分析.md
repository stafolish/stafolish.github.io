---
layout: post
title:  NioEventLoopGroup源码分析
date:   2019-03-17 20:05:00 +0800
categories: netty
tag: NIO
---

* content
{:toc}

#### NioEventLoopGroup源码分析

在此之前首先要明白：

- ##### netty是一个nio的框架。

- ##### 它是为了解决NIO编程的复杂性而存在的。

所以先学好NIO再来看netty就知道它的设计有多么精巧了，netty中NioEventLoop,ByteBuf,Channel分别对应着NIO编程的三个核心Selector,ByteBuffer,Channel,只不过netty对NIO的三个组件都有一定程度的封装和优化。要看懂netty源码，还要有一定的多线程的基础，并且需要了解一些常用设计模式如工厂模式，策略模式，责任链模式等等。

   

下面代码是一个经典的netty服务端代码，第一行代码就是创建一个NioEventLoopGroup对象，这个bossGroup的主要作用就是接受来自客户端的连接，然后将连接转给第二个EventLoopGroup（即workGroup）进行处理。	

```java
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerSocket {

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MyServerInitializer());
            ChannelFuture future = bootstrap.bind(8888).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }


    }
}
```

- ##### 	netty为何在服务启动辅助类（ServerBootstrap）中要设置两个EventLoopGroup呢？


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

- ##### 当不指定构造函数时，NioEventLoopGroup会创建多少个线程？

答案是CPU的逻辑核数*2，源码为证。	

```java
private static final int DEFAULT_EVENT_LOOP_THREADS;

    static {
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));

        if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.eventLoopThreads: {}", DEFAULT_EVENT_LOOP_THREADS);
        }
    }

protected MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
    }

```

这里io.netty.eventLoopThreads是配置在System.properties中的一个属性



- ##### NioEventLoopGroup的最终初始化在哪里完成？

NioEventLoopGroup最后会在父类MultithreadEventExecutorGroup中进行一系列的初始化操作。最后会调用到如下构造函数。

```java
protected MultithreadEventExecutorGroup(int nThreads, Executor executor,
                                            EventExecutorChooserFactory chooserFactory, Object... args) {
        if (nThreads <= 0) {
            throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
        }

        if (executor == null) {
            executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
        }

        children = new EventExecutor[nThreads];

        for (int i = 0; i < nThreads; i ++) {
            boolean success = false;
            try {
                children[i] = newChild(executor, args);
                success = true;
            } catch (Exception e) {
                // TODO: Think about if this is a good exception type
                throw new IllegalStateException("failed to create a child event loop", e);
            } finally {
                if (!success) {
                    for (int j = 0; j < i; j ++) {
                        children[j].shutdownGracefully();
                    }

                    for (int j = 0; j < i; j ++) {
                        EventExecutor e = children[j];
                        try {
                            while (!e.isTerminated()) {
                                e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                            }
                        } catch (InterruptedException interrupted) {
                            // Let the caller handle the interruption.
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }

        chooser = chooserFactory.newChooser(children);

        final FutureListener<Object> terminationListener = new FutureListener<Object>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                if (terminatedChildren.incrementAndGet() == children.length) {
                    terminationFuture.setSuccess(null);
                }
            }
        };

        for (EventExecutor e: children) {
            e.terminationFuture().addListener(terminationListener);
        }

        Set<EventExecutor> childrenSet = new LinkedHashSet<EventExecutor>(children.length);
        Collections.addAll(childrenSet, children);
        readonlyChildren = Collections.unmodifiableSet(childrenSet);
    }
```

因为默认传进来的Executor是null，所以会在这个构造函数中进入

```java
if (executor == null) {
            executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
        }
```

ThreadPerTaskExecutor实现了java.util.concurrent.Executor，默认必须传入一个线程工厂。

```java
public final class ThreadPerTaskExecutor implements Executor {
    private final ThreadFactory threadFactory;

    public ThreadPerTaskExecutor(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory");
        }
        this.threadFactory = threadFactory;
    }

    @Override
    public void execute(Runnable command) {
        threadFactory.newThread(command).start();
    }
}
```

这里使用Executor和ThreadFactory组合的模式使线程本身的创建和线程要执行任务的定义解耦。JDK中Exetor上的注释如下：

```
An object that executes submitted Runnable tasks. This interface provides a way of decoupling task submission from the mechanics of how each task will be run, including details of thread use, scheduling, etc. An Executor is normally used instead of explicitly creating threads. For example, rather than invoking new Thread(new(RunnableTask())).start() for each of a set of tasks, you might use:
Executor是一个用于执行提交的Runnable任务的对象，这个接口提供了一种将任务提交与每个任务的具体运行机制进行解耦的方式，包括如何具体的使用线程，任务调度等等。一个Executor通常用于代替显示的创建thread，比如不是用new Thread(new(RunnableTask())).start()启动一个一个的任务，而是以下面的方式：
Executor executor = anExecutor;
   executor.execute(new RunnableTask1());
   executor.execute(new RunnableTask2());
   ...

However, the Executor interface does not strictly require that execution be asynchronous. In the simplest case, an executor can run the submitted task immediately in the caller's thread:
Executor并不一定要异步的执行线程，最简单的例子，你可以如下直接调用run()方法而不是start()
 class DirectExecutor implements Executor {
   public void execute(Runnable r) {
     r.run();
   }
 }
More typically, tasks are executed in some thread other than the caller's thread. The executor below spawns a new thread for each task.
更常见的情况，任务是被异步的执行而不是在调用者的线程中执行。如同下面的executor，为每一个任务创建一个新的线程
 class ThreadPerTaskExecutor implements Executor {
   public void execute(Runnable r) {
     new Thread(r).start();
   }
 }

Many Executor implementations impose some sort of limitation on how and when tasks are scheduled. The executor below serializes the submission of tasks to a second executor, illustrating a composite executor.
许多Executor的实现是给任务何时和如何调度添加一些限制，下面的executor将任务串行化后提交给了第二个executor，展示了一个组合的executor（将Runnable放进一个队列中，然后一个个执行队列中的Runnable）。
 class SerialExecutor implements Executor {
   final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
   final Executor executor;
   Runnable active;

   SerialExecutor(Executor executor) {
     this.executor = executor;
   }

   public synchronized void execute(final Runnable r) {
     tasks.offer(new Runnable() {
       public void run() {
         try {
           r.run();
         } finally {
           scheduleNext();
         }
       }
     });
     if (active == null) {
       scheduleNext();
     }
   }

   protected synchronized void scheduleNext() {
     if ((active = tasks.poll()) != null) {
       executor.execute(active);
     }
   }
 }
The Executor implementations provided in this package implement ExecutorService, which is a more extensive interface. The ThreadPoolExecutor class provides an extensible thread pool implementation. The Executors class provides convenient factory methods for these Executors.
Memory consistency effects: Actions in a thread prior to submitting a Runnable object to an Executor happen-before its execution begins, perhaps in another thread.
内存一致性影响：在将一个Runnable对象提交给执行器的操作一定happen-before执行操作，或许是在另一个线程中。

```



- ##### ThreadPerTaskExecutor实现使用了命令模式和代理模式。

命令模式：我需要你帮我做一些事情，但是你不需要知道这件事情或者任务是怎么定义的，我只要把我定义的任务扔给你，你给我执行就好了。

代理模式：本来由ThreadPerTaskExecutor来执行的操作代理给了ThreadFactory对象。

netty真是博大精深，简简单单的几行代码使用了两种设计模式。

ThreadPerTaskExecutor实现了每一个任务起一个线程。