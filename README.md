# QuickServer快速构建API
** 用Java快速提供想要的API，那就快来试试QuickServer框架吧 **  
** 熟练使用QuickServer，对于你到Spring相关产品的使用迁移有不可描述的便利 **

## 个人用
QuickServer使用JavaNIO实现了简单的WebServer，可为快速提供API做好准备,你可以像下面这样来使用：
```java
public class DemoProdect{
    public static void main(String[] args){
        Quick.boot(DemoProdect.class, args); // 这里提供一个Boot类
    }
    
    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }
}
```
编译并运行它，它会根据默认配置监听在`8908`端口，并为URI`/hello`与函数`public String DemoProdect.hello()`之间做好了映射.

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