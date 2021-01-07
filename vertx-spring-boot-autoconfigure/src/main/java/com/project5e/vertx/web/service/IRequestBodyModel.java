package com.project5e.vertx.web.service;

import io.vertx.core.json.JsonObject;

public interface IRequestBodyModel {

    JsonObject formatJson(JsonObject json);

}
