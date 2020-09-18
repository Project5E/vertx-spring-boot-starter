package com.project5e.vertx.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class SampleApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SampleApplication.class, args);
        log.info("main done");
    }

//    @Primary
//    @Bean("iSubtractServiceVertxEBProxy")
//    public ISubtractService iSubtractService(Vertx vertx){
//        return new ISubtractServiceVertxEBProxy(vertx, SubtractService.class.getAnnotation(VertxService.class).address());
//    }
//
//    @Primary
//    @Bean("iPlusServiceVertxEBProxy")
//    public IPlusService iPlusService(Vertx vertx){
//        return new IPlusServiceVertxEBProxy(vertx, PlusService.class.getAnnotation(VertxService.class).address());
//    }

}
