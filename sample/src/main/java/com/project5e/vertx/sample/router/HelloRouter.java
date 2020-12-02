package com.project5e.vertx.sample.router;

import com.project5e.vertx.sample.router.dto.Query;
import com.project5e.vertx.web.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Router
@Tag(name = "ttp")
public class HelloRouter {

    @Resource
    Vertx vertx;
    @Resource
    PgPool pgPool;

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

    @GetMapping("/hello7")
    public void hello7(@RequestParam List<Integer> pages, Promise<JsonObject> promise) {
        JsonObject resp = new JsonObject();
        pgPool.query("select count(*) from public.banner").execute().onComplete(ar -> {
            if (ar.succeeded()) {
                long count = 0;
                for (Row row : ar.result()) {
                    count = row.getLong(0);
                }
                resp.put("count", count);
                if(count > 0) {
                    pgPool.preparedQuery("select " +
                            "  public.banner.id, " +
                            "  public.banner.cover, " +
                            "  public.banner.route, " +
                            "  public.banner.enabled, " +
                            "  public.banner.creator_id as creatorId, " +
                            "  cast(public.banner.created_time as timestamp) as createdTime, " +
                            "  public.banner.updater_id as updaterId, " +
                            "  cast(public.banner.updated_time as timestamp) as updatedTime " +
                            " from public.banner " +
                            " order by public.banner.sort_val asc " +
                            " limit $1").execute(Tuple.of(20))
                            .onComplete(ar1 -> {
                                if (ar1.succeeded()) {
                                    RowSet<Row> result = ar1.result();
                                    List<JsonObject> list = new ArrayList<>(result.size());
                                    for (Row row : result) {
                                        list.add(row.toJson());
                                    }
                                    promise.complete(resp.put("list", list));
                                } else {
                                    promise.fail(ar1.cause());
                                }
                            });
                }
            } else {
                promise.fail(ar.cause());
            }
        });
    }

}
