package com.project5e.vertx.sample.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@ProxyGen
@VertxGen
public interface ISubtractService {

    void subtract(Integer a, Integer b, Handler<AsyncResult<Integer>> resultHandler);

}
