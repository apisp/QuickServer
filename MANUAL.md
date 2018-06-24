# Quick Server 开发手册
## 一、概述
#### 1.注解驱动开发的便利
1. `@Get` `@Post` `@Put` `@Delete` `@Variable` 为RESTful API开发助力。
2. `@CrossDomain`允许某个API跨域使用，可以作用在方法、控制器类、启动类。
3. `@Scanning`指定所有的控制器类。只能作用在启动类。
4. `@ResponseType`指定响应数据类型。只能作用在方法。

#### 2.关于Mapping方法
1. 参数按需动态注入。你可以添加的参数类型有：`HttpRequest` `HttpResponse` `WebContext` `BodyBinary`。 你也可以用注解标注其他的参数，`@Variable`标注的参数会被注入请求路径相应的变量。
2. 返回值即是响应内容。你可以用`@ResponseType`指定响应内容的类型，默认是`application/json`。返回值类型可以是void，这时你需要在参数`HttpResponse`中指定响应的数据。

#### 3.示例
```java
public class Acknowledge {
    String name;
    String allCookies;
    String sessionid;
    String requestBody;
    Boolean assertTrue;
    Integer serverPort;
    
    // ... 省略getters、setters
}
```

```java
@CrossDomain // 允许本应用提供的API跨域使用
public class Demo {
    public static void main(String[] args) {
        Quick.boot(Demo.class, args); // 这里可以指定Boot类，默认是main函数所在类
    }

    @Get("/{name}")
    public Acknowledge hello(@Variable("name") String name, @Variable("version") String version,
            HttpRequest req, HttpResponse resp, BodyBinary body, WebContext ctx) {
        Acknowledge acknowledge = new Acknowledge();
        acknowledge.allCookies = req.header("Cookie");
        acknowledge.sessionid = req.cookie("sessionid") == null ? "" : req.cookie("sessionid").value();
        acknowledge.requestBody = req.body().toString();
        acknowledge.assertTrue = req.body() == body;
        acknowledge.serverPort = (Integer) ctx.setting("server.port");
        acknowledge.name = name;

        if (req.cookie("sessionid") == null) {
            resp.cookie("sessionid", IDs.uuid());
        }
        resp.header("My-Header", "some checkcode");

        return acknowledge; // 响应内容
    }
}
```

## 二、具体章节
![QuickServer Arch](https://raw.githubusercontent.com/apisp/resources/master/quick-server-arch.png)
### 1.核心
撰写中...
### 2.IOC
#### 注解介绍
`Singleton` 作用在类上。系统启动时自动实例化并缓存到单例容器。需要有默认构造函数，被标注的类可以使用 `@Autowired` 注解

`Controller` 作用在类上。系统启动时自动实例化并缓存到单例容器，并且记录其中的映射关系，和`启动类` 标识的注解  `@Scanning` 功能有重合。需要有默认构造函数，被标注的类可以使用 `@Autowired`注解。

`@Factory` 作用在类上。系统启动时，扫描所有此注解的类，实例化并依次执行 `@Accept` 注解标注的函数，函数返回值作为单例对象缓存到单例容器，被标注 `@Accept`的方法可以使用其他缓存了的单例对象，由函数参数注入即可。需要有默认构造函数。

`@Accept` 作用在方法上。被标注函数的返回值可以缓存到单例容器。

`@Autowired` 作用在类属性上。自动注入单例容器里的对象。默认按类型注入。
