package com.project5e.vertx.sample

import com.project5e.vertx.sample.configuration.log
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class SampleKtApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val context = SpringApplication.run(SampleKtApplication::class.java, *args)
            log.info("main done")
        }
    }
}