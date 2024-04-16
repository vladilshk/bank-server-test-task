package ru.vovai.bankserver.http;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.*;

public class HttpParserTest {

    @Test
    public void testGetRequestNoBody() throws IOException {
        String request = "GET /example HTTP/1.1\r\n" +
                "Host: www.example.com\r\n" +
                "User-Agent: Mozilla/5.0\r\n" +
                "Accept: text/html\r\n" +
                "\r\n";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes());
        HttpRequest httpRequest = HttpRequestParser.getHttpRequestFromStream(inputStream);

        assertEquals("GET", httpRequest.getMethod());
        assertEquals("/example", httpRequest.getUri());
        assertEquals("HTTP/1.1", httpRequest.getVersion());

        Map<String, String> headers = httpRequest.getHeaders();
        assertEquals("www.example.com", headers.get("Host"));
        assertEquals("Mozilla/5.0", headers.get("User-Agent"));
        assertEquals("text/html", headers.get("Accept"));

        assertNull(httpRequest.getBody());
    }

    @Test
    public void testPostRequestWithJsonBody() throws IOException {
        String request = "POST /api/data HTTP/1.1\r\n" +
                "Host: www.example.com\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 16\r\n" +
                "\r\n" +
                "{\"key\": \"value\"}";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes());
        HttpRequest httpRequest = HttpRequestParser.getHttpRequestFromStream(inputStream);

        assertEquals("POST", httpRequest.getMethod());
        assertEquals("/api/data", httpRequest.getUri());
        assertEquals("HTTP/1.1", httpRequest.getVersion());

        Map<String, String> headers = httpRequest.getHeaders();
        assertEquals("www.example.com", headers.get("Host"));
        assertEquals("application/json", headers.get("Content-Type"));
        assertEquals("16", headers.get("Content-Length"));

        assertEquals("{\"key\": \"value\"}", httpRequest.getBody());
    }

    @Test
    public void testInvalidRequest() {
        String invalidRequest = "InvalidRequest";

        InputStream inputStream = new ByteArrayInputStream(invalidRequest.getBytes());

        assertThrows(IllegalArgumentException.class, () -> HttpRequestParser.getHttpRequestFromStream(inputStream));
    }
}