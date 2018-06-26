# Quick Server 开发手册
## 一、概述
#### 1.注解驱动开发的便利
1. `@Get` `@Post` `@Put` `@Delete` `@Variable` 为RESTful API开发助力。
2. `@CrossDomain`允许某个API跨域使用，可以作用在方法、控制器类、启动类。
3. `@Scanning`指定所有的控制器类。只能作用在启动类。
4. `@ResponseType`指定响应数据类型。只能作用在方法。
5. `@View` 视图方式响应。只能作用于方法。

#### 2.关于Mapping方法
1. 参数按需动态注入。你可以添加的参数类型有：`org.json.JSONObject` `HttpRequest` `HttpResponse` `WebContext` `BodyBinary`，还有任何其它缓存在上下文容器中的对象。 你也可以用注解来标注方法中的某些参数。如，`@Variable`标注的参数会被注入请求路径相应的变量。
2. 返回值即是响应内容。你可以用`@ResponseType`指定响应内容的类型。默认是`application/json`，期望用`JSONObject`对象做返回值。返回值类型可以是void，这时你需要在参数`HttpResponse`中指定响应的数据。
3. `@View`标注的方法，会响应一个视图，返回值作为文件名，`@View` 注解值作为类路径下视图的根文件夹。

#### 3.示例
```java
@Controller
@CrossDomain // 此类所提供的API均支持跨域
public class DemoAPI {

    @Autowired("charset") // 从上下文读取配置并注入
    private String charset;

    @Post("/data")
    public JSONObject getData(JSONObject body) {
        return body;
    }

    @Get("/application/{name}")
    public JSONObject application(@Variable("name") String name, WebContext ctx, HttpRequest req, HttpResponse resp,
            BodyBinary body) {
        JSONObject obj = new JSONObject();
        obj.put("cookie", req.header("Cookie"));
        HttpCookie session = req.cookie("sessionid");
        if (Objects.isNull(session)) {
            resp.cookie("sessionid", Strings.uuid());
        } else {
            obj.put("sessionid", session.value());
        }
        obj.put("assertTrue", req.body() == body);
        obj.put("server.port", ctx.setting("server.port"));
        obj.put("server.charset", charset);
        obj.put("name", name);
        return obj;
    }
}
```

## 二、具体章节
![QuickServer Arch](https://raw.githubusercontent.com/apisp/resources/master/quick-server-arch.png)
`图片 - 1`

### 1.核心
##### 一些特性
接口`net.apisp.quick.core.ContextPreparation`作用是在系统缓存所有需要缓存的对象后，执行的一个自定义准备工作。你可以自己实现一些准备工作，系统会自动扫描并执行。

撰写中...
### 2.IOC
##### 注解介绍
`@Singleton` 作用在类上。系统启动时自动实例化并缓存到单例容器。需要有默认构造函数，被标注的类可以使用 `@Autowired` 注解

`@Controller` 作用在类上。系统启动时自动实例化并缓存到单例容器，并且记录其中的映射关系，和`启动类` 标识的注解  `@Scanning` 功能有重合。需要有默认构造函数，被标注的类可以使用 `@Autowired`注解。

`@Factory` 作用在类上。系统启动时，扫描所有此注解的类，实例化并依次执行 `@Accept` 注解标注的函数，函数返回值作为单例对象缓存到单例容器，被标注 `@Accept`的方法可以使用其他缓存了的单例对象，由函数参数注入即可。需要有默认构造函数。

`@Accept` 作用在方法上。被标注函数的返回值可以缓存到单例容器。

`@Autowired` 作用在类属性上。自动注入单例容器里的对象。默认按类型注入。

撰写中...
### 3.内建支持控制台
![QuickServer Buildin Support](https://raw.githubusercontent.com/apisp/resources/master/support.png)
`图片 - 2`

框架有内置的支持页面，它会有些框架的说明、注意事项，以及相关资讯。请访问 `/_quick.html`，查看详情。
`QuickServer` 跑起来后，可以动态添加补丁而不用停止服务。你可以编写`net.apisp.quick.ContextEnhancer`实现类，或者是新增的`Controller`类，编译为`.class`后放入  `QuickServer`应用的类路径下，进入`内置支持控制台`打补丁。