package com.project5e.vertx.web.component;

import cn.hutool.http.ContentType;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import lombok.Data;

@Data
public class ResponseEntity<T> {

    private int status = HttpResponseStatus.OK.code();
    private String statusMessage = HttpResponseStatus.OK.reasonPhrase();
    private MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    private T payload;
    private ContentType contentType = ContentType.JSON;

    public static <T> ResponseEntity<T> complete() {
        return complete(HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase());
    }

    public static <T> ResponseEntity<T> complete(int status) {
        return complete(status, null);
    }

    public static <T> ResponseEntity<T> complete(int status, String statusMessage) {
        ResponseEntity<T> entity = new ResponseEntity<>();
        entity.setStatus(status);
        entity.setStatusMessage(statusMessage);
        return entity;
    }

    public static <T> ResponseEntity<T> completeWithJson(Object json) {
        return completeWithJson(HttpResponseStatus.OK.code(), json);
    }

    public static <T> ResponseEntity<T> completeWithJson(int status, Object json) {
        ResponseEntity entity = complete(status);
        entity.getHeaders().add(HttpHeaders.CONTENT_TYPE.toString(), ContentType.JSON.getValue());
        entity.setPayload(json);
        entity.setContentType(ContentType.JSON);
        return entity;
    }

    public static ResponseEntity<String> completeWithPlainText(String text) {
        ResponseEntity<String> entity = new ResponseEntity<>();
        entity.headers.add(HttpHeaders.CONTENT_TYPE.toString(), ContentType.TEXT_PLAIN.getValue());
        entity.setPayload(text);
        entity.setContentType(ContentType.TEXT_PLAIN);
        return entity;
    }

}
