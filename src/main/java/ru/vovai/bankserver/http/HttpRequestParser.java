package ru.vovai.bankserver.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestParser {

    public static HttpRequest getHttpRequestFromStream(InputStream inputStream) throws IOException {
        HttpRequest httpRequest = new HttpRequest();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        // Чтение строки запроса
        String requestLine = readRequestLine(bufferedReader);
        parseRequestLine(httpRequest, requestLine);

        // Чтение и парсинг заголовков
        Map<String, String> headerMap = readAndParseHeaders(bufferedReader);
        httpRequest.setHeaders(headerMap);

        // Чтение тела запроса, если присутствует
        readBody(bufferedReader, httpRequest);

        return httpRequest;
    }

    private static String readRequestLine(BufferedReader bufferedReader) throws IOException {
        String line = bufferedReader.readLine();
        if (line == null) {
            throw new IOException("Missing request line");
        }
        return line;
    }

    private static void parseRequestLine(HttpRequest httpRequest, String requestLine) {
        String[] splitRequestLine = requestLine.split(" ");
        if (splitRequestLine.length == 3) {
            httpRequest.setMethod(splitRequestLine[0]);
            httpRequest.setUri(splitRequestLine[1]);
            httpRequest.setVersion(splitRequestLine[2]);
        } else {
            throw new IllegalArgumentException("Invalid request line format");
        }
    }

    private static Map<String, String> readAndParseHeaders(BufferedReader bufferedReader) throws IOException {
        Map<String, String> headerMap = new HashMap<>();
        String line;
        while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
            String[] keyValue = line.split(": ");
            if (keyValue.length == 2) {
                headerMap.put(keyValue[0], keyValue[1]);
            } else {
                // Обработка неверного формата заголовка
                throw new IllegalArgumentException("Invalid header format");
            }
        }
        return headerMap;
    }

    private static void readBody(BufferedReader bufferedReader, HttpRequest httpRequest) throws IOException {
        Map<String, String> headers = httpRequest.getHeaders();
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            if (contentLength > 0) {
                StringBuilder body = new StringBuilder();
                for (int i = 0; i < contentLength; i++) {
                    int character = bufferedReader.read();
                    if (character == -1) {
                        throw new IOException("Unexpected end of input while reading request body");
                    }
                    body.append((char) character);
                }
                httpRequest.setBody(body.toString());
            }
        }
    }
}
