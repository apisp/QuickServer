# QuickServer快速构建API
** 用Java快速提供想要的API，那就快来试试QuickServer框架吧 **  
** 熟练使用QuickServer，对于你到Spring相关产品的使用迁移有不可描述的便利 **

## 个人用
QuickServer使用JavaNIO实现了简单的WebServer，可为快速提供API做好准备,你可以像下面这样来使用：

###### 1.新建Java源码文件Demo.java
```java
import net.apisp.quick.annotation.*;
import net.apisp.quick.core.*;
import net.apisp.quick.core.http.*;

@CrossDomain // 允许本应用提供的API跨域使用
public class Demo {
    public static void main(String[] args) throws InterruptedException {
        new Quick(args).boot(); // 这里可以指定Boot类，默认是main函数所在类
    }

    @GetMapping("/")
    @ResponseType(ContentTypes.TXT) // 默认是JSON
    public String hello(HttpRequest req, HttpResponse resp, BodyBinary body, WebContext ctx) {
        StringBuilder acknowledge = new StringBuilder();
        acknowledge.append("Cookie string: ").append(req.header("Cookie")).append('\n');
        acknowledge.append("Cookie 'libai': ").append(req.cookie("libai").value()).append('\n');
        acknowledge.append("Request body string: ").append(req.body().toString()).append('\n');
        acknowledge.append("Assert is true: ").append(req.body() == body).append('\n');
        
        // 配置文件里的配置项，QuickServer监听的端口
        acknowledge.append("Server's port: ").append(ctx.setting("server.port"));
        
        resp.cookie("libai", "李白带节奏");
        resp.header("authcode", "自定义响应头");

        return acknowledge.toString(); // 响应内容
    }
}
```
###### 2.编译并运行它
```
$ javac -cp .;quick-server-1.4.jar Demo.java
$ java  -cp .;quick-server-1.4.jar Demo
```
它会根据默认配置监听在`8908`端口，并为URI`/`与函数`public String DemoProdect.hello()`之间做好了映射.

默认配置是 @ujued 的偏好设置，你可以在`classpath`提供一份设置`优先配置 quick.properties`， 下面是默认配置的镜像：
```
logging.level=INFO
server=net.apisp.quick.server.DefaultQuickServer
server.port=8908
server.threads=24
server.tmp.dir=${user.dir}
```
配置中的`server`项，可能会使你心生疑惑，它是个这样的一个类：

1. 它继承自`net.apisp.quick.server.QuickServer`，你可以用一些成熟的Server产品，如Jetty、Tomcat等来代替默认的QuickServer。你只需自己实现一个`net.apisp.quick.server.QuickServer`，并配置到配置文件`server`节点即可。
2. 能获取到应用运行的上下文`net.apisp.quick.server.var.ServerContext`。

上下文提供这些信息：`URI与逻辑函数的映射关系`，`一个线程池(池大小由配置文件决定)`， `配置信息`。

打开你的Java IDE，尽情发挥吧！