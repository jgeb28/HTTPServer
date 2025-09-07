package io.github.jgeb28;

import io.github.jgeb28.models.RequestLine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    public static final int PORT = 34522;
    public static void main(String[] args) {
        try(ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                try (
                        Socket socket = server.accept();
                        BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
                ) {
                    RequestLine l = parseRequestLine(input);
                    Map<String,String> map = parseHeaders(input);
                    System.out.println(l.httpVersion());
                    System.out.println(l.requestTarget());
                    System.out.println(l.method());
                    System.out.println(map);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private static RequestLine parseRequestLine(BufferedInputStream input) throws IOException, IllegalArgumentException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int prev = -1, curr;

        while ((curr = input.read()) != -1) {
            if (prev == '\r' && curr == '\n') {
                break;
            }
            buffer.write(curr);
            prev = curr;
        }

        String requestLineString = buffer.toString(StandardCharsets.US_ASCII);
        String[] arr = requestLineString.trim().split("\\s+");
        Pattern methodPattern = Pattern.compile("[A-Z0-9!#$%&'*+-.^_`|~]+");
        Matcher methodValidator = methodPattern.matcher(arr[0]);
        if(!methodValidator.matches())
            throw new IllegalArgumentException("Invalid Request Line Method format");
        if(!arr[2].equals("HTTP/1.1"))
            throw new IllegalArgumentException("Invalid Request Line HTTP version format");
        return new RequestLine(arr[0], arr[1], arr[2]);
    }

    private static Map<String, String> parseHeaders(BufferedInputStream input) throws IOException, IllegalArgumentException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int prev = -1, prev2 = -1,  curr;
        Map<String, String> headers = new HashMap<>();

        while ((curr = input.read()) != -1) {
            if (prev2 == '\n' && prev == '\r' && curr == '\n') {
                break;
            }
            buffer.write(curr);
            prev2 = prev;
            prev = curr;
        }

        String requestLineString = buffer.toString(StandardCharsets.US_ASCII);
        String[] arr = requestLineString.trim().split("\\r?\\n");
        Pattern fieldLinePattern = Pattern.compile("[A-Za-z0-9!#$%&'*+-.^_`|~]+");
        for (var line : arr) {
            if (line.isEmpty())
                continue;
            System.out.println(line);
            int colonIndex = line.indexOf(':');
            if(colonIndex == -1)
                throw new IllegalArgumentException("Invalid Header format");
            String fieldName = line.substring(0, colonIndex).trim();
            String token = line.substring(colonIndex + 1).trim();
            Matcher fieldLineValidator = fieldLinePattern.matcher(fieldName);
            if (!fieldLineValidator.matches())
                throw new IllegalArgumentException("Invalid field-line format");
            if(headers.containsKey(fieldName)) {
                headers.compute(fieldName, (k, val) -> val + ", " + token);
            } else {
                headers.put(fieldName, token);
            }
        }

        return headers;
    }
}
