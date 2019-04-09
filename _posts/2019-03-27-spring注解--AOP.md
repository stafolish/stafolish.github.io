---
layout: post
title:  spring注解--AOP
date:   2019-03-27 10:54:00 +0800
categories: spring
tag: spring注解
---



## Spring-AOP

AOP:【动态代理】



### 一、基础部分

在程序运行期间动态的将某段代码切入到指定方法指定位置进行运行的编程方式。

切面类里面的方法需要动态感知被代理的里里面的方法运行到哪里然后执行。



通知方法：

​	前置通知：(@Before)logStart

​	后置通知：(@After)logEnd

​	返回通知：(@AfterReturning)logReturn

​	异常通知：(@AfterThrowing)logException

​	环绕通知：(@Arround)动态代理，手动推进目标方法运行（jionPoint.procced()）

切面：@Aspect

开启基于注解的AOP：@EnableAspectjAutoProxy

在Spring中有很多的@EnableXXX；都是用来替代配置文件开启某项功能





- 使用方式：

1. 将业务逻辑组件和切面类都加入到容器中，并告诉Spring那个是切面类（@Aspect）
2. 在切面类上的每一个通知方法上标注通知注解，告诉Spring何时何地运行（切入点表达式）
3. 开启基于注解的aop模式：@EnableAspecitJAutoProxy





- 代码演示

切面类(示例需要导入lombok，logback，slf4j)：

```java
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Aspect
@Slf4j
public class LogAspect {

    //其他切面类可以引用
    @Pointcut("execution(public * com.xmg.learn.spring.MathCalculator.*(..))")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void logStart(JoinPoint joinPoint) {
        log.info("{}运行。。。参数列表是{}", joinPoint.getSignature().getName(), Arrays.asList(joinPoint.getArgs()));
    }

    @After("pointCut()")
    public void logEnd(JoinPoint joinPoint) {
        log.info("{}结束。。。@After", joinPoint.getSignature().getName());
    }

    @AfterReturning(value = "pointCut()", returning = "result")
    public void logReturn(JoinPoint joinPoint, Object result) {
        log.info("{}正常返回。。。@AfterReturning运行结果{}", joinPoint.getSignature().getName(),result);
    }

    //JoinPoint一定要出现在参数列表的第一位
    @AfterThrowing(value = "pointCut()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Exception exception) {
        log.info("{}异常。。。@AfterThrowing异常信息：{}",joinPoint.getSignature().getName(),exception);
    }
}
```

配置类：

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@Configuration
public class ConfigOfAop {

    @Bean
    public MathCalculator mathCalculator(){
        return new MathCalculator();
    }


    @Bean
    public LogAspect logAspect(){
        return new LogAspect();
    }
}
```



```java
public class MathCalculator {

    public int div(int i, int j) {

        System.out.println("MathCalculator...div...");
        return i / j;
    }
}
```

```java
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AOPTest {

    @Test
    public void test01(){
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ConfigOfAop.class);

        MathCalculator mathCalculator = applicationContext.getBean(MathCalculator.class);

        mathCalculator.div(12,3);
    }

}
```

运行结果：

```
2019-04-01 20:58:43,893 div运行。。。参数列表是[12, 3]
MathCalculator...div...
2019-04-01 20:58:43,932 div结束。。。@After
2019-04-01 20:58:43,933 除法正常返回。。。@AfterReturning运行结果div
```





------



### 二、原理部分

主要是看给容器中注册了什么组件，这个组件什么时候工作，包括组件工作时候的功能是什么

1. @Import(AspectJAutoProxyRegistrar.class):给容器中导入AspectJAutoProxyRegistrar

​	利用AspectJAutoProxyRegistrar自定义给容器中注册bean

​	该bean在容器中定义的名称为：org.springframework.aop.config.internalAutoProxyCreator

​	对应的实例为：AnnotationAwareAspectJAutoProxyCreator

给容器中注册一个AnnotationAwareAspectJAutoProxyCreator ：注解装配模式的切面自动代理创建器

2. AnnotationAwareAspectJAutoProxyCreator

   继承关系：

   ![]({{ '/styles/images/spring/aop/creator.PNG' | prepend: site.baseurl }})

3. AbstractAutoProxyCreator.setBeanFactory
4. AbstractAutoProxyCreator的后置处理器逻辑
5. AbstractAdvisorAutoProxyCreator.setBeanFactory-->initBeanFactory



#### 流程

1. 传入配置类，创建IOC容器

2. 注册配置类，调用refresh()刷新容器

3. registerBeanPostProcessors(beanFactory);注册bean的后置处理器来方便拦截bean的创建

   1. 先获取ioc容器中已经定义了的需要创建对象的所有BeanPostProcessor

   2. 给容器中添加别的BeanPostProcessor

   3. 优先注册实现了Priority接口的BeanPostProcessor，再给容器中注册实现了Ordered接口的BeanPostProcessor，最后注册没有实现优先级接口的BeanPostProcessor

   4. 所谓的注册是什么，注册BeanPostProcessor，实际上就是创建BeanPostProcessor对象，然后保存在容器中；

      创建internalAutoProxyCreator的BeanPostProcessor【AnnotationAwareAspectJAutoProxyCreator】

      1. 创建bean的实例

      2. populateBean；给bean的属性赋值

      3. initializeBean：初始化bean：

         1. invokeAwareMethods：处理awre接口方法回调

         2. applyBeanPostProcessorsBeforeInitialization：调用后置处理器的postProcessBeforeInitialization

         3. invokeInitMethods：执行自定义初始化方法（在@Bean注解中定义的init方法）

         4. applyBeanPostProcessorsAfterInitialization：调用后置处理器的postProcessAfterInitialization

            ##### postProcessor的before和after的执行逻辑如上所述

      4. BeanPostProcessor(AnnotationAwareAspectJAutoProxyCreator)创建成功，并且调用inittBeanFactory方法；创建aspectJAdvisorsBuilder

   5. 把BeanPostProcessor注册到BeanFactory中；beanFactory.addBeanPostProcessor(postProcessor);

      

      ##### ===========以上是创建和注册AnnotationAwareAspectJAutoProxyCreator的过程=============

      

   6. AnnotationAwareAspectJAutoProxyCreator=>InstantiationAwareBeanPostProcessor

4. finishBeanFactoryInitialization(beanFactory);完成BeanFactory初始化工作，所谓初始化其实就是创建剩下的单实例bean。之所以说是剩下的是因为IOC容器的组件比如一些BeanPostProcessor在注册BeanPostProcessors时就已经创建完毕了

   1. 遍历获取容器中所有Bean，依次创建对象getBean(beanName);

      getBean->doGetBean->getSingleton->

   2. 创建bean

      【AnnotationAwareAspectJAutoProxyCreator在所有bean创建之前会有一个拦截，因为它是InstantiationAwareBeanPostProcessor，会调用postProcessBeforeInstantiation()方法】

      1. 先从缓存中获取当前bean，如果能获取到，说明bean时之前本创建过的，那么拿过来直接使用，否则再创建（单实例机制，只要被创建过的bean都会被缓存起来）

      2. createBean()；创建bean；先拿到bean定义信息(是否是单实例的)，已及bean类型定义解析保存等等**【BeanPostProcessor是Bean在创建完成初始化前后调用的】**

         **【InstantiationAwareBeanPostProcessor是在创建bean实例之前先尝试用后置处理器返回对象的】**

         1. resolveBeforeInstantiation(beanName, mbdToUse);解析BeforeInstantiation希望后置处理器在此能返回一个代理对象，如果能返回代理对象就使用，如果不能就继续

            1. 后置处理器先尝试返回对象

               ```java
               //拿到所有后置处理器，如果是InstantiationAwareBeanPostProcessor；就执行postProcessBeforeInstantiation
               bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
               
               if (bean != null) {
                  bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
               }
               ```

               

         2. doCreateBean(beanName, mbdToUse, args);真正创建一个bean实例和**3.3**流程一样

AnnotationAwareAspectJAutoProxyCreator【InstantiationAwareBeanPostProcessor】的作用：

1. 在每一个bean创建之前调用postProcessBeforeInstantiation()

   关心MathCalculatorhe LogAspect的创建

   1. 判断当前的bean是否在advisedBeans中（保存了所有需要增强的bean）

   2. 判断当前bean是否是基础类型的Advice，PointCut，Advisor，AopInfrastructureBean，或者是否是切面（判断是否有标注@Aspect注解）

   3. 是否需要跳过

      1. 获取候选的增强器（切面中的通知方法）【List<Advisor> candidateAdvisors】

         每一个封装通知方法的增强器是InstantiationModelAwarePointcutAdvisor

         判断每一个增强器是否是AspectJPointcutAdvisor类型，返回true

      2. 永远返回false

2. 创建对象--->

   调用postProcessAfterInitialization--->

   ​	return wrapIfNecessary(bean, beanName, cacheKey);//包装如果需要的情况下

   1. 获取当前bean所有增强器（通知方法）Object[] specificInterceptors

      1. 找到候选的所有增强器findAdvisorsThatCanApply（找哪些通知方法是需要欺辱当前bean方法的）
      2. 获取到能在bean使用的增强器。
      3. 给增强器排序

   2. 保存当前bean在advisedBeans中

   3. 如果当前bean需要增强，创建当前bean的代理对象

      1. 获取所有增强器

      2. 保存到proxyFactory

      3. 创建代理对象：Spring自动决定

         ##### JdkDynamicAopProxy(config) ;jdk动态代理

         ##### ObjenesisCglibAopProxy(config);cglib动态代理

   4. 给容器中返回当前组件使用cglib增强了的代理对象

   5. 以后容器中获取到的就是这个组件的代理对象，执行目标方法的时候，代理对象就会执行通知方法的流程

3. 目标方法执行：

   容器中保存了组件的代理对象（cglib增强后的对象），这个对象里面保存了详细信息（比如增强器，目标对象）

   1. CglibAopProxy.intercept();拦截目标方法的执行

   2. 根据proxyFacotory 对象获取目标发方法将要执行的拦截器链this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

      1. List<Object> interceptorList = new ArrayList<>(advisors.length);保存所有的拦截器，

         一个默认的org.springframework.aop.interceptor.ExposeInvocationInterceptor.ADVISOR和4个增强器（before,after,afterreturning,afterthrowing）

      2. 遍历所有的增强器，将其转为Inteceptor；registry.getInterceptors(advisor);

      3. 将增强器转为List<MethodInterceptor>

         如果是MethodInterceptor，直接加入到集合中

         如果不是，使用AdvisorAdapter将增强器转为MethodInterceptor

         转换完成返回MethodInterceptor数组

   3. 如果没有拦截器链直接执行目标方法

      拦截器（每一个通知方法又被包装成为方法拦截器，利用MethodInterceptor机制）

   4. 如果有拦截器链，目标方法，拦截器链等信息传入创建一个CglibMethodInvocation对象，并调用Object retVal = mi.proceed();

   5. 拦截器的触发过程；

      1. 如果没有拦截执行目标方法，或者拦截器的索引和拦截器数组-1大小一样（指定到了最后一个拦截器）执行目标方法
      2. 连使获取每一个拦截器，拦截器执行invoke方法，每一个拦截器等待下一个拦截器执行返回以后再来执行；拦截器链的机制，把这通知方法和目标方法的执行顺序



#### 总结：

1. @EnableAspectJAutoProxy开启AOP功能

2. @EnableAspectJAutoProxy会给容器中注册一个组件AnnotationAwareAspectJAutoProxyCreator

3. AnnotationAwareAspectJAutoProxyCreator是一个后置处理器

4. 容器的创建流程：

   1. 注册后置处理器：registerBeanPostProcessors(beanFactory);创建AnnotationAwareAspectJAutoProxyCreator对象

   2. 初始化剩下的单实例bean：finishBeanFactoryInitialization(beanFactory);

      1. 创建业务逻辑组件和切面组件

      2. AnnotationAwareAspectJAutoProxyCreator拦截创建过程

      3. 组件创建完之后，判断是否需要增强

         是：切面的通知方法，包装成增强器（Advisor）；给业务逻辑创建一个代理对象（cglib）

5. 执行目标方法：

   1. 代理对象执行目标方法

   2. CglibAopProxy.intercept();

      1. 得到目标方法的拦截器链（增强器包装成拦截器MethodInterceptor）

      2. 利用拦截器的链式机制，依次进入每一个拦截器执行

      3. 效果：

         正常执行：前置通知-->目标方法-->后置通知-->返回通知

         异常执行：前置通知-->目标方法-->后置通知-->异常通知

