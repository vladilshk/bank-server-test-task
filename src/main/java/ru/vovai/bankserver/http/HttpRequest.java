package ru.vovai.bankserver.http;

import lombok.Data;

import java.util.Map;

@Data
public class HttpRequest {

    private String method;

    private String uri;

    private String version;

    private Map<String, String> headers;

    private String body;
}
