package io.github.jgeb28.models;

public record RequestLine(
        String httpVersion,
        String requestTarget,
        String method
){}
