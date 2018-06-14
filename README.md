# QuickServer快速构建API
*用Java快速提供想要的API，那就快来试试QuickServer框架吧 *
*熟练使用QuickServer，对于你到Spring相关产品的使用迁移有不可描述的便利*

## 个人用
QuickServer已经实现了一个简单的WebServer，可为快速提供API做好准备，可以部署为微服务。你可以按下面步骤来使用：

#### 1.新建Java源码文件Demo.java
```java
import net.apisp.quick.annotation.GetMapping;
import net.apisp.quick.core.Quick;

public class Demo {
    public static void main(String[] args) {
        Quick.boot(args);
    }

    @GetMapping("/")
    public String hello() {
        return "Hello World";
    }
}
```
*如果要基于默认配置正常运行，该文件需要用UTF-8编码*

#### 2.编译并运行
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

默认配置是 @ujued 的偏好设置，你可以在`classpath`提供一份优先配置`quick.properties`， 下面是默认配置的镜像：
```
charset=UTF-8
logging.level=INFO
server=net.apisp.quick.server.DefaultQuickServer
server.port=8908
server.threads=24
server.tmp.dir=${user.dir}
```
你可以覆盖这些默认配置。当然优先级最高的还是从command传进来的args。配置中的`server`项，可能会使你心生疑惑，它是个这样的一个类：

1. 它继承自`net.apisp.quick.server.QuickServer`，你可以用一些成熟的Server产品，如Jetty、Tomcat等来代替默认的QuickServer。你只需自己实现一个`net.apisp.quick.server.QuickServer`，并配置到配置文件`server`节点即可。
2. 能获取到应用运行的上下文`net.apisp.quick.server.var.ServerContext`。

上下文提供这些信息：`URI与逻辑函数的映射关系`，`一个线程池`， `配置信息`。

最后，还是打开你的Java IDE，尽情发挥吧！

你还可以翻阅一下 [开发手册](MANUAL.md)