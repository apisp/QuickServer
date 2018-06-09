# QuickServer快速构建API
** 用Java快速提供想要的API，那就快来试试QuickServer框架吧 **

## 个人用
QuickServer使用JavaSocket实现了简单的WebServer，可为快速提供API做好准备,你可以像下面这样来使用：
```java
public class DemoProdect{
    public static void main(String[] args){
        Quick.run(DemoProdect.class);
    }
    
    @GetMapping("/hello")
    public String hello(){
        return "{\"message\":\"Hello World\"}";
    }
}
```
编译并运行它，它会根据默认配置监听在`8908`端口，并为URI`/hello`与函数`public String DemoProdect.hello()`之间做好了映射.

默认配置是 @ujued 的偏好设置，你可以在`classpath`提供一份设置`优先配置 application.properties`， 下面是默认配置的镜像：
```
quick.logging.level=INFO
quick.server=net.apisp.quick.server.simple.SimpleServer
quick.server.port=8908
quick.server.threads=24
```
配置中的`quick.server`项，可能会使你心生疑惑，它是个这样的一个类：

1. 它继承自`net.apisp.quick.server.QuickServer`
2. 能获取到应用运行的上下文`net.apisp.quick.core.ServerContext`。

上下文提供这些信息：`URI与逻辑函数的映射关系`，`一个线程池(池大小由配置文件决定)`。

打开你的Java IDE，尽情发挥吧！