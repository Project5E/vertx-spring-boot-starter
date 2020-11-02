package com.project5e.vertx.sample.router;

import com.project5e.vertx.web.annotation.HttpMethod;
import com.project5e.vertx.web.annotation.RequestMapping;
import com.project5e.vertx.web.annotation.Router;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Router
@Tag(name = "ttp")
public class HelloRouter {

    @RequestMapping(value = "/hello", method = HttpMethod.GET)
    public Future<String> hello() {
        return Future.succeededFuture("hello");
    }

    @RequestMapping(value = "/hello1", method = HttpMethod.GET)
    public void hello1(Promise<String> resultPromise) {
        resultPromise.complete("hello1");
    }

}
