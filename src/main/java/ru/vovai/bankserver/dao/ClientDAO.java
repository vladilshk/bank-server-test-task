package ru.vovai.bankserver.dao;

import ru.vovai.bankserver.exception.DatabaseException;
import ru.vovai.bankserver.exception.InsufficientFundsException;
import ru.vovai.bankserver.exception.UserAlreadyExistsException;
import ru.vovai.bankserver.exception.UserNotFoundException;
import ru.vovai.bankserver.model.Client;

import java.sql.*;

import static ru.vovai.bankserver.mapper.ClientMapper.mapResultSetToClient;

public class ClientDAO {

    private final String url;
    private final String username;
    private final String password;

    public ClientDAO(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    // Метод для получения соединения с базой данных
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }


    // Метод для создания таблицы client
    public void createClientTable() throws DatabaseException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS client (" +
                    "id SERIAL PRIMARY KEY," +
                    "login VARCHAR(255) UNIQUE NOT NULL," +
                    "password VARCHAR(255) NOT NULL," +
                    "balance BIGINT DEFAULT 0" +
                    ")";
            statement.executeUpdate(sql);
            System.out.println("Table 'client' created successfully");
        } catch (SQLException e) {
            throw new DatabaseException("Error creating client table", e);
        }
    }


    // Метод для создания пользователя
    public void createUser(String name, String password) throws DatabaseException, UserAlreadyExistsException {
        try (Connection connection = getConnection()) {
            // Проверяем, существует ли пользователь с указанным именем
            if (userExists(connection, name)) {
                throw new UserAlreadyExistsException("User with name '" + name + "' already exists");
            }

            // Если пользователь не существует, создаем нового
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO client (login, password, balance) VALUES (?, ?, 1000)")) {
                statement.setString(1, name);
                statement.setString(2, password);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseException("Error creating user", e);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting connection", e);
        }
    }

    private boolean userExists(Connection connection, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS count FROM client WHERE login = ?")) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                return count > 0;
            }
        }
        return false;
    }

    // Метод для получения баланса пользователя
    public int getBalance(String login) throws UserNotFoundException, DatabaseException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT balance FROM client WHERE login = ?")) {
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("balance");
            } else {
                throw new UserNotFoundException("User not found");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting balance", e);
        }
    }

    // Метод для безопасного перевода средств с одного аккаунта на другой
    public synchronized void transfer(String from, String to, int amount) throws InsufficientFundsException, UserNotFoundException, DatabaseException {
        // Получаем баланс отправителя
        int balanceFrom = getBalance(from);
        if (balanceFrom < amount) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // Вычитаем сумму из баланса отправителя
        try (Connection connection = getConnection();
             PreparedStatement updateStatement = connection.prepareStatement("UPDATE client SET balance = balance - ? WHERE login = ?");
             PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM client WHERE login = ?")) {
            connection.setAutoCommit(false);

            updateStatement.setInt(1, amount);
            updateStatement.setString(2, from);
            updateStatement.executeUpdate();

            // Проверяем, существует ли получатель
            selectStatement.setString(1, to);
            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                // Если получатель существует, добавляем сумму к его балансу
                PreparedStatement updateToStatement = connection.prepareStatement("UPDATE client SET balance = balance + ? WHERE login = ?");
                updateToStatement.setInt(1, amount);
                updateToStatement.setString(2, to);
                updateToStatement.executeUpdate();
                connection.commit();
            } else {
                connection.rollback(); // Откатываем транзакцию, если получатель не найден
                throw new UserNotFoundException("Recipient not found");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error transferring funds", e);
        }
    }

    // Метод для получения пользователя по логину
    public Client getUserByLogin(String login) throws UserNotFoundException, DatabaseException {
        String sql = "SELECT * FROM client WHERE login = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToClient(resultSet); // Используем маппер для создания объекта пользователя
            } else {
                throw new UserNotFoundException("User with login '" + login + "' not found");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting user by login", e);
        }
    }
}
