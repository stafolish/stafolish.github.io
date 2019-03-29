---
layout: post
title:  spring注解--ioc容器
date:   2019-03-28 17:23:00 +0800
categories: spring
tag: 注解

---

#### Spring IOC相关注解：

1. ##### 组件：@Conponent一族：@Service，@Controller，@Repository，@Configuration……
2. ##### 包扫描：

```java
/**
 * ComponentScan：包扫描
 * excludeFilters：排除策略
 * includeFilters：包含（与useDefaultFilters = false配合使用）
 * ComponentScans:可指定多个扫描规则
 * FilterType.ANNOTATION:按照注解
 * FilterType.ASSIGNABLE_TYPE：指定类型
 * FilterType.CUSTOM:自定义
 */
@ComponentScan(value = "com.xmg.learn.spring",
        includeFilters = {
//@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Controller.class, Service.class}),
//@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = BookService.class),
                @ComponentScan.Filter(type = FilterType.CUSTOM,classes = MyTypeFilter.class)
        }, useDefaultFilters = false)
public class Config {

    @Bean("person")
    public Person person01() {
        return new Person("zhangsan", 20);
    }
}
```
```java
/**
 * 自定义扫描过滤策略
 */
public class MyTypeFilter implements TypeFilter {
    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        //获取当前类注解的信息
        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
        //获取当前正在扫描的类的信息
        ClassMetadata classMetadata = metadataReader.getClassMetadata();
        //获取当前类资源（类的路径）
        Resource resource = metadataReader.getResource();
        String className = classMetadata.getClassName();
        if (className.contains("er")){
            return true;
        }
        System.out.println("--------->"+className);

        return false;
    }
}
```
```java
    @Test
    public void test01(){
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(Config.class);
        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            System.out.println(name);
        }
    }

    /**
     * test01输出结果:
     *--------->com.xmg.learn.spring.Main
     * --------->com.xmg.learn.spring.config.Config2
     * --------->com.xmg.learn.spring.dao.BookDao
     * org.springframework.context.annotation.internalConfigurationAnnotationProcessor
     * org.springframework.context.annotation.internalAutowiredAnnotationProcessor
     * org.springframework.context.event.internalEventListenerProcessor
     * org.springframework.context.event.internalEventListenerFactory
     * config
     * person
     * myTypeFilter
     * bookController
     * bookService
     */
```

##### 3.配置类注解：@Configuration--官方注释:Indicates that a class declares one or more @Bean methods and may be processed by the Spring container to generate bean definitions and service requests for those beans at runtime

表示此类可以声明一个或多个@bean方法，并且可以由Spring容器处理，以便在运行时为这些bean生成bean定义和服务请求

##### 4.添加第三方包里的bean：@Bean:默认以方法名作为bean的id，也可以指定id如@Bean("person")。

##### 5.懒加载：@Lazy以懒加载的方式创建bean（默认会在容器启动时就创建）。	

##### 6.作用域：@scope：常用有两种方式：ConfigurableBeanFactory.SCOPE_PROTOTYPE

###### 					            ConfigurableBeanFactory.SCOPE_SINGLETON

```java
@Configuration
public class Config2 {
    /**
     * ConfigurableBeanFactory#SCOPE_PROTOTYPE:多实例，每次获取创建一个对象
     * ConfigurableBeanFactory#SCOPE_SINGLETON:单实例，ioc容器启动调用方法创建
     *
     * 懒加载：SCOPE_SINGLETON默认在容器启动时创建对象
     *          懒加载：容器第一次获取时再创建对象
     *
     */

//    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Lazy
    @Bean
    public Person person(){
        System.out.println("----------给容器添加Person");
        return new Person("lala",16);
    }    
}
```
```java

    @Test
    public void test02(){
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(Config2.class);
        System.out.println("ioc容器创建完成");
        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            System.out.println(name);
        }
        Object person = context.getBean("person");
        Object person2 = context.getBean("person");
        System.out.println(person==person2);
    }
```
```java
    /**
     * test02输出结果：
     * ioc容器创建完成
     * org.springframework.context.annotation.internalConfigurationAnnotationProcessor
     * org.springframework.context.annotation.internalAutowiredAnnotationProcessor
     * org.springframework.context.event.internalEventListenerProcessor
     * org.springframework.context.event.internalEventListenerFactory
     * config2
     * person
     * ----------给容器添加Person
     * true
     */
```

##### 7.条件注册：@Conditional：根据条件注册bean，可以加在类上或方法上。

```java
@Conditional({LinuxCondition.class})
@Bean("linus")
public Person person02(){
    return new Person("Linus", 46);
}

@Conditional({WindowsCondition.class})
@Bean("bill")
public Person person01(){
    return new Person("Bill Gates",62);
}

```

```java
public class LinuxCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        //获取ioc使用的beanfactory
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        //类加载器
        ClassLoader classLoader = context.getClassLoader();
        //获取当前环境信息
        Environment environment = context.getEnvironment();
        //获取bean定义的注册类
        BeanDefinitionRegistry registry = context.getRegistry();
        boolean definition = registry.containsBeanDefinition("person");


        String property = environment.getProperty("os.name");
        if (property.contains("linux")){
            return true;
        }
        return false;
    }
}
```

```java
public class WindowsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        String property = environment.getProperty("os.name");
        if (property.contains("Windows")){
            return true;
        }
        return false;
    }
}

```

##### 8.快速向Spring容器中添加组件：@Import，可以添加多个类，同样也可以配合@Lazy/@Scope使用。

用法：

​	1.@Import({XXX.class})；容器自动注册这个组件，id默认是全类名。

​	2.ImportSelector：返回需要导入的组件的全类名数组；

​	3.ImportBeanDefinitionRegistrar：手工注册Bean到容器中

```java
@Configuration
@Import({Color.class, Red.class, MyImportSelector.class})
public class Config2 {
}
```
```java
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Color {
    public Color() {
        System.out.println("===========");
    }
}
```
```java
public class MyImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {

        return new String[]{"com.xmg.learn.spring.bean.Yellow","com.xmg.learn.spring.bean.Blue"};
    }
}
```

```java
public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        boolean definition = registry.containsBeanDefinition("com.xmg.learn.spring.bean.Red");
        boolean definition2 = registry.containsBeanDefinition("com.xmg.learn.spring.bean.Blue");

        if (definition && definition2){
            //指定Bean的定义信息（Bean的类型。。。）
            BeanDefinition bd = new RootBeanDefinition(RainBow.class);
            //注册一个Bean，指定Bean名
            registry.registerBeanDefinition("rainBow",bd);
        }
    }
}
```
##### 9.工厂Bean方式：FactoryBean

```java
public class ColorFactoryBean implements FactoryBean<Color> {
    @Override
    public Color getObject() throws Exception {
        System.out.println("getObject");
        return new Color();
    }

    @Override
    public Class<?> getObjectType() {
        return Color.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
```

```java
@Bean
public ColorFactoryBean colorFactoryBean(){
    return new ColorFactoryBean();
}
```


```java
    @Test
    public void testImport() {
        printBeans(context);
        Blue blue = context.getBean(Blue.class);
        System.out.println(blue);
        Object colorFactoryBean = context.getBean("colorFactoryBean");
        System.out.println("bean type: " + colorFactoryBean.getClass());
        Object colorFactoryBean2 = context.getBean("colorFactoryBean");
        System.out.println(colorFactoryBean == colorFactoryBean2);

        Object colorFactoryBean3 = context.getBean("&colorFactoryBean");
        System.out.println("bean type: " + colorFactoryBean3.getClass());
    }
	public void printBeans(AnnotationConfigApplicationContext context) {
        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            System.out.println(name);
        }
    }
```
```
打印结果：
org.springframework.context.annotation.internalConfigurationAnnotationProcessor
org.springframework.context.annotation.internalAutowiredAnnotationProcessor
org.springframework.context.event.internalEventListenerProcessor
org.springframework.context.event.internalEventListenerFactory
config2
com.xmg.learn.spring.bean.Color
com.xmg.learn.spring.bean.Red
com.xmg.learn.spring.bean.Yellow
com.xmg.learn.spring.bean.Blue
person
bill
colorFactoryBean
rainBow
com.xmg.learn.spring.bean.Blue@60f00693
getObject
bean type: class com.xmg.learn.spring.bean.Color
true
bean type: class com.xmg.learn.spring.bean.ColorFactoryBean
```



#### 向容器中添加组件的方式

- 包扫描+组件标识注解（@Service/@Controller/@Repository/@Component）
- @Bean（导入第三方包里的组件）
- @Import（快速给容器中导入一个组件）

  - @Import({XXX.class})；容器自动注册这个组件，id默认是全类名。

  - ImportSelector：返回需要导入的组件的全类名数组；

  - ImportBeanDefinitionRegistrar：手工注册Bean到容器中
- 使用Spring提供的FactoryBean(工厂Bean)
  - 默认获取到的是FactoryBean调用getObject创建的对象
  - 要获取FactoryBean本身，需要给id前加一个&
```

```