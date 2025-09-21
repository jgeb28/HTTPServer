package io.github.jgeb28;

import io.github.jgeb28.models.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ResponseUtils {

    public static void WriteStatusLine(OutputStream out, HttpStatus status) throws IOException {
        String statusLine = "HTTP/1.1 " + status.code() + " " + status.reason() + "\r\n";

        out.write(statusLine.getBytes(StandardCharsets.UTF_8));
    }

    public static Map<String, String> getDefaultHeaders(int contentLength) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Length", String.valueOf(contentLength));
        headers.put("Connection", "close");
        headers.put("Content-Type", "text/plain");

        return headers;
    }

    public static Map<String, String> getDefaultHeadersChunkedEncoding() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        headers.put("Transfer-Encoding", "chunked");

        return headers;
    }

    public static void WriteHeaders(OutputStream out, Map<String, String> headers) throws IOException {
        StringBuilder builder = new StringBuilder(256);
        for (var entry : headers.entrySet()) {
            builder.append(entry.getKey());
            builder.append(": ");
            builder.append(entry.getValue());
            builder.append("\r\n");
        }
        builder.append("\r\n");

        out.write(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void writeChunks(OutputStream out, InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            String chunkSize = Integer.toHexString(bytesRead) + "\r\n";
            out.write(chunkSize.getBytes(StandardCharsets.UTF_8));
            out.write(buffer, 0, bytesRead);
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }

        out.write("0\r\n".getBytes(StandardCharsets.UTF_8));
    }

    public static void writeTrailers(OutputStream out, Map<String, String> trailers) throws IOException {
        StringBuilder builder = new StringBuilder(256);
        for (var entry : trailers.entrySet()) {
            builder.append(entry.getKey());
            builder.append(": ");
            builder.append(entry.getValue());
            builder.append("\r\n");
        }

        out.write(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

}
