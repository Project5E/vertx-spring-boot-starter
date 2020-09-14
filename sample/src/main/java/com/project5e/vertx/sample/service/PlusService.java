package com.project5e.vertx.sample.service;

import com.project5e.vertx.core.autoconfigure.VertxProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Slf4j
public class PlusService implements NumberService {

    @Autowired
    VertxProperties vertxProperties;

    @PostConstruct
    public void init() {
        log.info(this.getClass().getTypeName() + " init!, vertxProperties = " + vertxProperties.toString());
    }

}
