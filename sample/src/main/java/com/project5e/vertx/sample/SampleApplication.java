package com.project5e.vertx.sample;

import com.project5e.vertx.sample.service.*;
import com.project5e.vertx.serviceproxy.annotation.VertxService;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@Slf4j
@SpringBootApplication
public class SampleApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SampleApplication.class, args);
        log.info("main done");
    }

    @Primary
    @Bean("iSubtractServiceVertxEBProxy")
    public ISubtractService iSubtractService(Vertx vertx){
        return new ISubtractServiceVertxEBProxy(vertx, SubtractService.class.getAnnotation(VertxService.class).address());
    }

    @Primary
    @Bean("iPlusServiceVertxEBProxy")
    public IPlusService iPlusService(Vertx vertx){
        return new IPlusServiceVertxEBProxy(vertx, PlusService.class.getAnnotation(VertxService.class).address());
    }

}
