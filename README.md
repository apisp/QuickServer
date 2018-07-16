# QuickServer快速构建API

* 需要用Java快速提供API，快来试试QuickServer框架吧
* 熟练使用QuickServer，对于到Spring相关产品的使用迁移有不可描述的便利
* 无任何第三方依赖，只有一个不到200KB的Jar包(内含org.json三方包)，满足你快速提供API

`IOC` `MVC` `RESTful` `零配置`

## 最佳用途
工作之余自己想实现点小创意？`SpringBoot ?` NO! 你可能需要你个极简的`Java方案`， 就是`Quick Server`。QuickServer已经实现了一个简单的WebServer，可为快速提供API做好准备。当然还有更多的实现，一切只为让你轻松实现小创意。

现在以 `Ajax` + `QuickServer API` ，开始吧！当然，他不止这些！

你的内心毫无波澜？加入我们，一起来开发 `Quick Server` 吧！

## 下面几个简单步骤开始你的奇妙之旅：

#### 1.新建Java源码文件Demo.java (基于默认配置正常运行，需要用UTF-8编码该文件)
```java
import net.apisp.quick.core.Quick;

public class Demo {
    public static void main(String[] args) {
        Quick.boot(args).mapping("GET /", () -> "Hello World");
    }
}
```

#### 2.编译并运行
类Uinx
```bash
$ javac -cp .:quick-server-1.4.jar Demo.java
$ nohup java -cp .:quick-server-1.4.jar Demo
```

Windows
```bash
C:\Users\xxx>javac -encoding UTF-8 -cp .;quick-server-1.4.jar Demo.java
C:\Users\xxx>javaw -Dfile.encoding=UTF-8 -cp .;quick-server-1.4.jar Demo
```

## 它干了什么
它首先会根据默认配置监听在 `8908` 端口，并为 `GET` 请求的URI `/` 与 `匿名函数` 之间做好了映射。你可以发 `HTTP协议` 的GET请求包来使用这个API了。你也可以使用函数加注解的方式来处理后台逻辑与URI的映射，发送过来的HTTP协议数据，解析完成后，在必要时就可以按需注入在映射函数中，映射函数也可以方便的修改响应的HTTP数据。

## 小提示
默认配置是 `@ujued` 的偏好设置，你可以在 `classpath` 提供一份优先配置 `quick.properties` ， 下面是默认配置的镜像：
```
charset=UTF-8
controller.exception.handler=net.apisp.quick.core.std.QuickExceptionHandler
logging.class=net.apisp.quick.log.ConsoleLog
logging.level=INFO
server=net.apisp.quick.server.DefaultQuickServer
server.port=8908
server.threads=24
server.tmp.dir=${user.dir}
```
你可以任意覆盖这些默认配置。当然优先级最高的还是从 `command` 传进来的 `args` 。配置中的 `server` 项，可能会使你心生疑惑，它是个这样的一个类：

1. 它继承自 `net.apisp.quick.server.QuickServer` ，你可以用一些成熟的Server产品，如`Jetty`、`Tomcat`等来代替默认的QuickServer。你只需自己实现一个 `net.apisp.quick.server.QuickServer` ，并配置到配置文件 `server` 节点即可。
2. 能获取到应用运行的上下文 `net.apisp.quick.server.var.ServerContext` 。

`ServerContext` 提供这些信息： `URI与逻辑函数的映射关系` ， `一个线程池` ，  `配置信息` 。

最后，还是打开你的Java IDE，尽情发挥吧！

## 你还可以
* [翻阅开发手册](MANUAL.md)  
* [看看官方Demo](https://gitee.com/ujued/DemoBasedQuickServer)
* [访问 APISP.NET](https://apisp.net)
* [加入 开发者的狂欢 QQ群](https://jq.qq.com/?_wv=1027&k=5ZVMI8a)