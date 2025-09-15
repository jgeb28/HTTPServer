package io.github.jgeb28;

import io.github.jgeb28.models.RequestLine;
import org.junit.Test;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class RequestUtilsTest {

    @Test
    public void givenValidRequestLine_WhenParsingRequestLine_ThenReturnRequestLine() {
        // given
        String text = "GET / HTTP/1.1\r\n";
        InputStream inputStream = new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)
        );
        // when
        try {
            RequestLine requestLine = RequestUtils.parseRequestLine(inputStream);
            Assert.assertEquals("GET", requestLine.method());
            Assert.assertEquals("/", requestLine.requestTarget());
            Assert.assertEquals("HTTP/1.1", requestLine.httpVersion());

        } catch (IOException e) {
            e.printStackTrace();
        }
        // then
    }

    @Test
    public void givenInvalidRequestLineMethodFormat_WhenParsingRequestLine_ThenThrowException() {
        // given
        String text = "GET@2 / HTTP/1.1\r\n";
        InputStream inputStream = new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)
        );
        // when & then
        Assert.assertThrows(IllegalArgumentException.class, ()->{
            RequestLine requestLine = RequestUtils.parseRequestLine(inputStream);
        });
    }

    @Test
    public void givenInvalidRequestLineVersionFormat_WhenParsingRequestLine_ThenThrowException() {
        // given
        String text = "GET@2 / HTTP/2.1\r\n";
        InputStream inputStream = new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)
        );
        // when & then
        Assert.assertThrows(IllegalArgumentException.class, ()->{
            RequestLine requestLine = RequestUtils.parseRequestLine(inputStream);
        });
    }

    @Test
    public void givenInvalidRequestLineFormat_WhenParsingRequestLine_ThenThrowException() {
        // given
        String text = "GET HTTP/1.1\r\n";
        InputStream inputStream = new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)
        );
        // when & then
        Assert.assertThrows(IllegalArgumentException.class, ()->{
            RequestLine requestLine = RequestUtils.parseRequestLine(inputStream);
        });
    }

    @Test
    public void givenValidHeaders_WhenParsingHeaders_ThenReturnHeaders() {
        // given
        String text =  "Host: localhost\r\n" +
                "User-Agent: SimpleTestClient/1.0\r\n" +
                "Accept: */*\r\n" +
                "\r\n";
        InputStream inputStream = new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)
        );
        // when
        try {
            Map<String,String> headers = RequestUtils.parseHeaders(inputStream);
            Assert.assertEquals("localhost", headers.get("host"));
            Assert.assertEquals("SimpleTestClient/1.0", headers.get("user-agent"));
            Assert.assertEquals("*/*", headers.get("accept"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        // then
    }

    @Test
    public void givenInvalidHeaderFormat_WhenParsingHeaders_ThenThrowException() {
        // given
        String text =  "Host localhost\r\n" +
                "User-Agent: SimpleTestClient/1.0\r\n" +
                "Accept: */*\r\n" +
                "\r\n";
        InputStream inputStream = new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)
        );
        // when & then
        Assert.assertThrows(IllegalArgumentException.class, ()->{
            Map<String,String> headers = RequestUtils.parseHeaders(inputStream);
        });
    }

    @Test
    public void givenInvalidFieldLineFormat_WhenParsingHeaders_ThenThrowException() {
        // given
        String text =  "Host@: localhost\r\n" +
                "User-Agent: SimpleTestClient/1.0\r\n" +
                "Accept: */*\r\n" +
                "\r\n";
        InputStream inputStream = new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)
        );
        // when & then
        Assert.assertThrows(IllegalArgumentException.class, ()->{
            Map<String,String> headers = RequestUtils.parseHeaders(inputStream);
        });
    }

    @Test
    public void givenValidBody_WhenParsingBody_ThenReturnBody() {
        // given
        int contentLength = 11;
        String text = "Ala ma kota";
        InputStream inputStream = new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)
        );
        // when
        try {
            byte[] body = RequestUtils.parseBody(inputStream, contentLength);
            Assert.assertEquals("Ala ma kota", new String(body, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // then
    }

    @Test
    public void givenSmallerContentLengthThanActual_WhenParsingBody_ThenThrowException() {
        // given
        int contentLength = 10;
        String text = "Ala ma kota";
        InputStream inputStream = new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)
        );
        // when & then
        Assert.assertThrows(IllegalArgumentException.class, ()->{
            byte[] body = RequestUtils.parseBody(inputStream, contentLength);
        });
    }

    @Test
    public void givenGreaterContentLengthThanActual_WhenParsingBody_ThenThrowException() {
        // given
        int contentLength = 12;
        String text = "Ala ma kota";
        InputStream inputStream = new ByteArrayInputStream(
                text.getBytes(StandardCharsets.UTF_8)
        );
        // when & then
        Assert.assertThrows(IllegalArgumentException.class, ()->{
            byte[] body = RequestUtils.parseBody(inputStream, contentLength);
        });
    }

}