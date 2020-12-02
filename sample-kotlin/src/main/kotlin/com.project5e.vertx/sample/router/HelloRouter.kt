package com.project5e.vertx.sample.router

import com.project5e.vertx.web.annotation.GetMapping
import com.project5e.vertx.web.annotation.Router
import io.swagger.v3.oas.annotations.tags.Tag
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import lombok.extern.slf4j.Slf4j
import java.util.*
import kotlin.coroutines.CoroutineContext

@Slf4j
@Router
@Tag(name = "ttp")
class HelloRouter(
        val vertx: Vertx,
        val pgPool: PgPool
) : CoroutineScope {

    override val coroutineContext: CoroutineContext by lazy { vertx.orCreateContext.dispatcher() }

    @GetMapping("/banners")
    fun getBanners(promise: Promise<JsonObject>) {
        val resp = JsonObject()
        pgPool.query("select count(*) from public.banner").execute().onComplete { ar ->
            if (ar.succeeded()) {
                var count: Long = 0
                for (row in ar.result()) {
                    count = row.getLong(0)
                }
                resp.put("count", count)
                if (count > 0) {
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
                            .onComplete { ar1 ->
                                if (ar1.succeeded()) {
                                    val result = ar1.result()
                                    val list: MutableList<JsonObject> = ArrayList(result.size())
                                    for (row in result) {
                                        list.add(row.toJson())
                                    }
                                    promise.complete(resp.put("list", list))
                                } else {
                                    promise.fail(ar1.cause())
                                }
                            }
                }
            } else {
                promise.fail(ar.cause())
            }
        }
    }

    @GetMapping("/banners1")
    fun getBanners1(promise: Promise<JsonObject>) {
        launch {
            val resp = JsonObject()
            val count: Long = pgPool.query("select count(*) from public.banner").execute()
                    .map { it.map { it.getLong(0) }.firstOrNull() ?: 0 }
                    .await()
            resp.put("count", count)
            if (count > 0) {
                val list = pgPool.preparedQuery("""
                    select 
                      "public"."banner"."id", 
                      "public"."banner"."cover", 
                      "public"."banner"."route", 
                      "public"."banner"."enabled", 
                      "public"."banner"."creator_id" as "creatorId", 
                      cast("public"."banner"."created_time" as timestamp) as "createdTime", 
                      "public"."banner"."updater_id" as "updaterId", 
                      cast("public"."banner"."updated_time" as timestamp) as "updatedTime"
                    from "public"."banner"
                    order by "public"."banner"."sort_val" asc
                    limit $1
                """).execute(Tuple.of(20))
                        .map { it.map { it.toJson() } }
                        .await()
                resp.put("list", list)
            }
            promise.complete(resp)
        }
    }
}