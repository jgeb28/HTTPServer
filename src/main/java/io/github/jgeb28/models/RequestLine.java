package io.github.jgeb28.models;

public record RequestLine(
        String method,
        String requestTarget,
        String httpVersion

){}
