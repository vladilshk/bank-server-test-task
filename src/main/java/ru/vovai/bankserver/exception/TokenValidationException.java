package ru.vovai.bankserver.exception;

public class TokenValidationException extends Exception {
    public TokenValidationException(String message) {
        super(message);
    }
}
