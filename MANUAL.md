# Quick Server 开发手册

#### 1.注解驱动开发的便利
`@GetMapping`
`@PostMapping`
`@PutMapping`
`@DeleteMapping`
`@PathVariable`
为RESTful API开发助力。
`@CrosDomain`允许某个API跨域使用，可以作用在方法、控制器类、启动类。
`@Scanning`指定所有的控制器类。只能作用在启动类。
`@ResponseType`指定响应数据类型。只能作用在方法。