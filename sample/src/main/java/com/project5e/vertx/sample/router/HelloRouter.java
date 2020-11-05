package com.project5e.vertx.sample.router;

import com.project5e.vertx.sample.router.dto.Query;
import com.project5e.vertx.web.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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

    @PostMapping("/hello2")
    public void hello2(@RequestBody JsonObject jsonObject, Promise<JsonObject> resultPromise) {
        resultPromise.complete(jsonObject);
    }

    @GetMapping("/hello3")
    public void hello3(Promise<Void> resultPromise) {
        resultPromise.complete();
    }

    @GetMapping("/hello4")
    public void hello4(@RequestBody Query query, Promise<Query> resultPromise) {
        resultPromise.complete(query);
    }

    @GetMapping("/hello5")
    public void hello5(@RequestParam Integer page, Promise<String> resultPromise) {
        resultPromise.complete("page = " + page);
    }

    @GetMapping("/hello6")
    public void hello6(@RequestParam List<Integer> pages, Promise<String> resultPromise) {
        resultPromise.complete("pages = " + pages);
    }

}
