package com.project5e.vertx.sample.service;

import com.project5e.vertx.autoconfigure.VertxProperties;
import com.project5e.vertx.core.annotation.VertxService;
import com.project5e.vertx.sample.verticle.NumberVerticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Slf4j
@VertxService(value = "plus.bus", register = NumberVerticle.class)
public class PlusService implements NumberService {

    @Autowired
    VertxProperties vertxProperties;

    @PostConstruct
    public void init() {
        log.info(this.getClass().getTypeName() + " init!, vertxProperties = " + vertxProperties.toString());
    }

}
