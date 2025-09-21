# Basic Java HTTP/1.1 Server

A simple HTTP/1.1 server implemented in Java without external dependencies (except for testing).  
Supports **reading requests**, **sending normal responses**, and **sending chunked responses** with optional trailers.

---

## Features

- **Read HTTP requests** (request line, headers, and optional body)
- **Handle GET requests** with route-based responses
- **Send normal responses** with `Content-Length`
- **Send chunked responses** (`Transfer-Encoding: chunked`)
- **Support optional trailers** in chunked responses
- **Basic error handling** for 400 and 500 HTTP status codes
- **Concurrent client handling** using a cached thread pool

---

## Example Routes

| Route         | Method | Description |
|---------------|--------|-------------|
| `/page`       | GET    | Returns a static HTML page |
| `/chunkText`  | GET    | Streams a text file in chunked encoding with an optional checksum trailer |
| `/chunkImage` | GET    | Streams an image file in chunked encoding |
| `/error400`   | GET    | Returns HTTP 400 Bad Request |
| `/error500`   | GET    | Returns HTTP 500 Internal Server Error |
| Any other     | GET    | Returns HTTP 404 Not Found |

---
