package com.project5e.vertx.sample.router;

import com.project5e.vertx.sample.router.dto.Query;
import com.project5e.vertx.web.annotation.GetMapping;
import com.project5e.vertx.web.annotation.RequestMapping;
import com.project5e.vertx.web.annotation.Router;
import com.project5e.vertx.web.component.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Router
@RequestMapping("/error")
@Tag(name = "error")
public class ErrorRouter {

    @GetMapping("/err1")
    public void err1(Promise<ResponseEntity<String>> promise) {
        promise.complete(ResponseEntity.completeWithPlainText("hhh"));
    }

    @GetMapping("/err2")
    public void err2(Promise<ResponseEntity<Query>> promise) {
        promise.complete(ResponseEntity.completeWithJson(300, new JsonObject().put("aa", 11)));
    }

}
