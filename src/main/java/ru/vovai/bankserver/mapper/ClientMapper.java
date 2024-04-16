package ru.vovai.bankserver.mapper;

import ru.vovai.bankserver.model.Client;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientMapper {

    public static Client mapResultSetToClient(ResultSet resultSet) throws SQLException {
        Client user = new Client();
        user.setId(resultSet.getLong("id"));
        user.setLogin(resultSet.getString("login"));
        user.setPassword(resultSet.getString("password"));
        user.setBalance(resultSet.getLong("balance"));
        return user;
    }
}
