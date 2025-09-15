package io.github.jgeb28;

import io.github.jgeb28.models.Request;
import io.github.jgeb28.models.RequestLine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                handleClient(server.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (
                BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
        ) {
            RequestLine requestLine = RequestUtils.parseRequestLine(input);
            Map<String,String> headers = RequestUtils.parseHeaders(input);
            String val = headers.get("content-length");
            int contentLength = 0;

            if (val != null && val.matches("\\d+")) {
                contentLength = Integer.parseInt(val);
            }
            byte[] body = contentLength > 0 ? RequestUtils.parseBody(input, contentLength) : null;

            Request request = new Request(requestLine, headers, body);

            System.out.println(request);
            System.out.println(new String(request.body(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
