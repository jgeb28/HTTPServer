package io.github.jgeb28;

import io.github.jgeb28.models.RequestLine;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestUtils {

    public static RequestLine parseRequestLine(InputStream input) throws IOException, IllegalArgumentException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int prev = -1, curr;

        while ((curr = input.read()) != -1) {
            if (prev == '\r' && curr == '\n') {
                break;
            }
            buffer.write(curr);
            prev = curr;
        }

        String requestLineString = buffer.toString(StandardCharsets.UTF_8);
        String[] arr = requestLineString.trim().split("\\s+");

        if (arr.length != 3)
            throw new IllegalArgumentException("Invalid request line format: Invalid number of arguments");

        Pattern methodPattern = Pattern.compile("[A-Z0-9!#$%&'*+-.^_`|~]+");
        Matcher methodValidator = methodPattern.matcher(arr[0]);
        if (!methodValidator.matches())
            throw new IllegalArgumentException("Invalid request line format: Invalid method format");

        if (!arr[2].equals("HTTP/1.1"))
            throw new IllegalArgumentException("Invalid request line format: Invalid HTTP version format");

        return new RequestLine(arr[0], arr[1], arr[2]);
    }

    public static Map<String, String> parseHeaders(InputStream input) throws IOException, IllegalArgumentException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int prev = -1, prev2 = -1, curr;
        Map<String, String> headers = new HashMap<>();

        while ((curr = input.read()) != -1) {
            if (prev2 == '\n' && prev == '\r' && curr == '\n') {
                break;
            }
            buffer.write(curr);
            prev2 = prev;
            prev = curr;
        }

        String fieldLineString = buffer.toString(StandardCharsets.UTF_8);
        String[] arr = fieldLineString.trim().split("\\r?\\n");

        Pattern fieldLinePattern = Pattern.compile("[A-Za-z0-9!#$%&'*+-.^_`|~]+");
        for (var line : arr) {
            if (line.isEmpty())
                continue;

            int colonIndex = line.indexOf(':');
            if (colonIndex == -1)
                throw new IllegalArgumentException("Invalid header format: Invalid line format");

            String fieldName = line.substring(0, colonIndex).trim().toLowerCase();
            String token = line.substring(colonIndex + 1).trim();

            Matcher fieldLineValidator = fieldLinePattern.matcher(fieldName);
            if (!fieldLineValidator.matches())
                throw new IllegalArgumentException("Invalid header format: Invalid field-line format");

            if (headers.containsKey(fieldName)) {
                headers.compute(fieldName, (k, val) -> val + ", " + token);
            } else {
                headers.put(fieldName, token);
            }
        }

        return headers;
    }

    public static byte[] parseBody(InputStream input, int contentLength) throws IOException, IllegalArgumentException {
        byte[] buffer = new byte[contentLength];

        int bytesRead = 0;
        while (bytesRead < contentLength) {
            int read = input.read(buffer, bytesRead, contentLength - bytesRead);
            if (read == -1) {
                throw new IllegalArgumentException("Invalid body format: Content-Length mismatch.");
            }
            bytesRead += read;
        }

        input.mark(1);
        int nextByte = input.read();
        if (nextByte != -1) {
            input.reset();
            throw new IllegalArgumentException("Invalid body format: Content-Length mismatch.");
        }

        return buffer;
    }
}
