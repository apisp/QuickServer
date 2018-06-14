# Quick Server 开发手册

#### 1.注解驱动开发的便利
1. `@GetMapping` `@PostMapping` `@PutMapping` `@DeleteMapping` `@PathVariable` 为RESTful API开发助力。
2. `@CrosDomain`允许某个API跨域使用，可以作用在方法、控制器类、启动类。
3. `@Scanning`指定所有的控制器类。只能作用在启动类。
4. `@ResponseType`指定响应数据类型。只能作用在方法。

#### 2.关于Mapping方法
1. 参数按需动态注入。你可以添加的参数类型有：`HttpRequest` `HttpResponse` `WebContext` `BodyBinary`。 你也可以用注解标注其他的参数，`@PathVariable`标注的参数会被注入请求路径相应的变量。
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

    @GetMapping("/{name}/{version}")
    public Acknowledge hello(@PathVariable("name") String name, @PathVariable("version") String version,
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