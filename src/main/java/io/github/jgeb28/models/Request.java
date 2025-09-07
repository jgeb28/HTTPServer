package io.github.jgeb28.models;

import java.util.Map;

public record Request(
    RequestLine requestLine,
    Map<String,String> headers,
    byte[] body
){ }

