package com.project5e.vertx.sample.service;

import com.project5e.vertx.core.autoconfigure.VertxProperties;
import com.project5e.vertx.sample.verticle.NumberVerticle;
import com.project5e.vertx.serviceproxy.annotation.VertxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Slf4j
@VertxService(address = "plus.bus", register = NumberVerticle.class)
public class PlusService implements NumberService {

    @Autowired
    VertxProperties vertxProperties;

    @PostConstruct
    public void init() {
        log.info(this.getClass().getTypeName() + " init!, vertxProperties = " + vertxProperties.toString());
    }

}
