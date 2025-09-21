package io.github.jgeb28;

import io.github.jgeb28.models.HttpStatus;
import io.github.jgeb28.models.Request;
import io.github.jgeb28.models.RequestLine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService executor;

    public Server(int port) {
        this.port = port;
        this.executor = Executors.newCachedThreadPool();
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = server.accept();
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (Socket socket = clientSocket) {
            BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
            OutputStream out = socket.getOutputStream();
            try {
                RequestLine requestLine = RequestUtils.parseRequestLine(input);
                Map<String, String> requestHeaders = RequestUtils.parseHeaders(input);
                String val = requestHeaders.get("content-length");
                int contentLength = 0;
                if (val != null && val.matches("\\d+")) {
                    contentLength = Integer.parseInt(val);
                } else if (val != null) {
                    throw new IllegalArgumentException("Content-Length mismatch");
                }
                byte[] body = contentLength > 0 ? RequestUtils.parseBody(input, contentLength) : null;

                Request request = new Request(requestLine, requestHeaders, body);

                if (request.requestLine().method().equals("GET")) {
                    switch (request.requestLine().requestTarget()) {
                        case "/error400" -> throw new IllegalArgumentException("You willingly created Bad Request");
                        case "/error500" -> throw new IOException();
                        case "/page" -> {
                            ResponseUtils.WriteStatusLine(out, HttpStatus.OK);
                            Map<String, String> responseHeaders = ResponseUtils.getDefaultHeaders(39);
                            responseHeaders.put("Content-Type", "text/html");
                            ResponseUtils.WriteHeaders(out, responseHeaders);

                            byte[] responseBody = ("<h1>Page</h1>\n<span>Some content</span>").getBytes(StandardCharsets.UTF_8);
                            out.write(responseBody);
                            out.flush();
                        }
                        case "/chunkText" -> {
                            ResponseUtils.WriteStatusLine(out, HttpStatus.OK);
                            Map<String, String> responseHeaders = ResponseUtils.getDefaultHeadersChunkedEncoding();
                            ResponseUtils.WriteHeaders(out, responseHeaders);

                            try (InputStream in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream("text.txt"))) {
                                ResponseUtils.writeChunks(out, in);
                            }

                            Map<String, String> trailers = new HashMap<>();
                            trailers.put("X-Content-SHA256", "2c621afd7c1e3cd868bc136ef0da912e196e7a0b178a5d543da79b5c78f1aaa4");
                            ResponseUtils.writeTrailers(out, trailers);

                            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                        case "/chunkImage" -> {
                            ResponseUtils.WriteStatusLine(out, HttpStatus.OK);
                            Map<String, String> responseHeaders = ResponseUtils.getDefaultHeadersChunkedEncoding();
                            responseHeaders.put("Content-Type", "image/jpeg");
                            ResponseUtils.WriteHeaders(out, responseHeaders);

                            try (InputStream in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream("image.jpeg"))) {
                                ResponseUtils.writeChunks(out, in);
                            }

                            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                        default -> sendError(out, HttpStatus.NOT_FOUND, "Resource not found");
                    }
                }


            } catch (IllegalArgumentException e) {
                sendError(out, HttpStatus.BAD_REQUEST, "Invalid request: " + e.getMessage());
            } catch (IOException e) {
                sendError(out, HttpStatus.INTERNAL_SERVER_ERROR, "Server error occurred");
                e.printStackTrace();
            } finally {
                input.close();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendError(OutputStream out, HttpStatus status, String message) throws IOException {
        byte[] body = message != null ? message.getBytes(StandardCharsets.UTF_8) : new byte[0];
        ResponseUtils.WriteStatusLine(out, status);
        Map<String, String> responseHeaders = ResponseUtils.getDefaultHeaders(body.length);
        ResponseUtils.WriteHeaders(out, responseHeaders);
        if (body.length > 0) {
            out.write(body);
        }
        out.flush();
    }

}
