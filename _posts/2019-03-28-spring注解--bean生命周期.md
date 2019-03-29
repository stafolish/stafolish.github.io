---
layout: post
title:  spring注解--bean生命周期
date:   2019-03-28 17:23:00 +0800
categories: spring
tag: spring注解
---

#### Spring Bean生命周期

bean创建——初始化——销毁过程

容器管理bean生命周期。

我们可以自定义初始化和销毁方法；容器在bean进行到当前生命周期时候来调用我们自定义的初始化和销毁方法

##### 创建：

​	单实例、多实例

##### 初始化：

​	对象创建完成，并赋值好，调用出事化方法。。。

##### 销毁：

​	单实例：容器关闭时调用

​	多实例：容器不会管理这个bean，容器不会调用销毁方法

- 指定初始化和销毁方法：

  - 通过@Bean定义init-method和destroy-method

- 通过Bean实现InitializingBean(定义初始化逻辑)，DisposableBean(定义销毁逻辑)

- 使用JSR250：

  - @PostConstruct：在bean创建完成并且属性赋值完成，来完成初始化；@PreDestroy：在容器销毁bean之前通知我们进行清理工作。

- BeanPostProcessor：bean的后置处理器

  - postProcessBeforeInitialization：在初始化之前
    postProcessAfterInitialization：在初始化之后

##### BeanPostProcessor原理：

```java
populateBean(beanName, mbd, instanceWrapper);//给Bean进行属性赋值
{
   wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
   invokeInitMethods(beanName, wrappedBean, mbd);//执行初始化
   wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
}
```

Spring 底层对bean进行创建及赋值的时候也大量使用了BeanPostProcessor。

如ApplicationContextAwareProcessor：处理实现了XxxAware的组件

AutowiredAnnotationBeanPostProcessor：处理@Autowired

InitDestroyAnnotationBeanPostProcessor：处理生命周期注解

BeanValidationPostProcessor：处理校验



