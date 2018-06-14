# Quick Server 开发手册

#### 1.注解驱动开发的便利
1. `@GetMapping` `@PostMapping` `@PutMapping` `@DeleteMapping` `@PathVariable` 为RESTful API开发助力。
2. `@CrosDomain`允许某个API跨域使用，可以作用在方法、控制器类、启动类。
3. `@Scanning`指定所有的控制器类。只能作用在启动类。
4. `@ResponseType`指定响应数据类型。只能作用在方法。

#### 2.关于Mapping方法
1. 参数按需动态注入。你可以添加的参数类型有：`HttpRequest` `HttpResponse` `WebContext` `BodyBinary`。 你也可以用注解标注其他的参数，`@PathVariable`标注的参数会被注入请求路径相应的变量。
2. 返回值即是响应内容。你可以用`@ResponseType`指定响应内容的类型，默认是`application/json`。返回值类型可以是void，这时你需要在参数`HttpResponse`中指定响应的数据。