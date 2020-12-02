package com.project5e.vertx.sample.configuration

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val Any.log: Logger get() = LoggerFactory.getLogger(this::class.java)
