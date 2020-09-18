# vertx-spring-boot-starter
为了简化 vertx 开发

# 功能
- 自动部署 Verticle (异步)
- 自动注册 Service

# 使用

vertx 最低版本 **vertxVersion = '4.0.0-milestone4**

## Core
```groovy
    implementation project(":vertx-spring-boot-starter")
    implementation "io.vertx:vertx-core:$vertxVersion"
```

```java
@Verticle
public class CalculateVerticle extends AbstractVerticle {
    // ...
}
```

## Service Proxy

```groovy
    implementation project(":vertx-serviceproxy-spring-boot-starter")
    implementation "io.vertx:vertx-core:$vertxVersion"
    implementation "io.vertx:vertx-service-proxy:$vertxVersion"
    annotationProcessor "io.vertx:vertx-service-proxy:$vertxVersion"
    annotationProcessor "io.vertx:vertx-codegen:$vertxVersion:processor"
```
```java
@ProxyGen
@VertxGen
public interface ICalculateService {

    void plus(Integer a, Integer b, Handler<AsyncResult<Integer>> resultHandler);

    void subtract(Integer a, Integer b, Handler<AsyncResult<Integer>> resultHandler);

}
```

```java
@VertxService(address = "calculate.bus", register = CalculateVerticle.class)
public class CalculateService implements ICalculateService {

    @Autowired
    IPlusService iPlusService;
    @Autowired
    ISubtractService iSubtractService;

    @Override
    public void plus(Integer a, Integer b, Handler<AsyncResult<Integer>> resultHandler) {
        iPlusService.plus(a, b, resultHandler);
    }

    @Override
    public void subtract(Integer a, Integer b, Handler<AsyncResult<Integer>> resultHandler) {
        iSubtractService.subtract(a, b, resultHandler);
    }
}
```
---
# License

TODO
