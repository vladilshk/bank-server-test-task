package ru.vovai.bankserver.exception;

import java.sql.SQLException;

public class DatabaseException extends Throwable {
    public DatabaseException(String message, SQLException e) {
        super(message, e);
    }
}
