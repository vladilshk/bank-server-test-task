package ru.vovai.bankserver.security;

import ru.vovai.bankserver.exception.TokenNotFoundException;
import ru.vovai.bankserver.http.HttpRequest;

import java.util.Map;

public class JwtUtil {

    public static String getTokenFromRequest(HttpRequest httpRequest) throws TokenNotFoundException {
        Map<String, String> headers = httpRequest.getHeaders();
        String authorization = "Authorization";
        if (headers.containsKey(authorization)) {
            String[] authHeaderParts = httpRequest.getHeaders().get(authorization).split(" ");
            if (authHeaderParts.length == 2 && authHeaderParts[0].equals("Bearer")) {
                return authHeaderParts[1];
            } else {
                throw new TokenNotFoundException("Invalid Authorization header format");
            }
        } else {
            throw new TokenNotFoundException("Authorization header not found");
        }
    }
}
