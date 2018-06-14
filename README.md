# QuickServer快速构建API
** 用Java快速提供想要的API，那就快来试试QuickServer框架吧 **  
** 熟练使用QuickServer，对于你到Spring相关产品的使用迁移有不可描述的便利 **

## 个人用
QuickServer已经实现了一个简单的WebServer，可为快速提供API做好准备，可以部署为微服务。你可以按下面步骤来使用：

###### 1.新建Java源码文件Demo.java
```java
import net.apisp.quick.annotation.CrossDomain;
import net.apisp.quick.annotation.GetMapping;
import net.apisp.quick.core.BodyBinary;
import net.apisp.quick.core.Quick;
import net.apisp.quick.core.WebContext;
import net.apisp.quick.core.http.HttpRequest;
import net.apisp.quick.core.http.HttpResponse;
import net.apisp.quick.util.IDs;

@CrossDomain // 允许本应用提供的API跨域使用
public class Demo {
    public static void main(String[] args) {
        Quick.boot(args); // 这里可以指定Boot类，默认是main函数所在类
    }

    @GetMapping("/")
    public String hello(HttpRequest req, HttpResponse resp, BodyBinary body, WebContext ctx) {
        StringBuilder acknowledge = new StringBuilder();
        acknowledge.append('{');
        acknowledge.append("\"all_cookies\": \"").append(req.header("Cookie")).append("\", ");
        acknowledge.append("\"sessionid\": \"")
                .append(req.cookie("sessionid") == null ? "" : req.cookie("sessionid").value()).append("\", ");
        acknowledge.append("\"req_body\": \"").append(req.body().toString()).append("\", ");
        acknowledge.append("\"assert_true\": ").append(req.body() == body).append(", ");
        acknowledge.append("\"server_port\": ").append(ctx.setting("server.port")).append(", ");
        acknowledge.append("\"message\": \"").append("Hello World").append("\"");
        acknowledge.append('}');

        if (req.cookie("sessionid") == null) {
            resp.cookie("sessionid", IDs.uuid());
        }
        resp.header("My-Header", "some checkcode");

        return acknowledge.toString(); // 响应内容
    }
}
```
###### 2.这样编译并运行它
类Uinx
```
$ javac -cp .:quick-server-1.4.jar Demo.java
$ nohup java -cp .:quick-server-1.4.jar Demo
```

Windows
```
C:\Users\xxx>javac -encoding UTF-8 -cp .;quick-server-1.4.jar Demo.java
C:\Users\xxx>javaw -Dfile.encoding=UTF-8 -cp .;quick-server-1.4.jar Demo
```
它会根据默认配置监听在`8908`端口，并为URI`/`与函数`public String Demo.hello(..)`之间做好了映射.

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