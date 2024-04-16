package ru.vovai.bankserver.dao;

import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.vovai.bankserver.exception.DatabaseException;
import ru.vovai.bankserver.exception.InsufficientFundsException;
import ru.vovai.bankserver.exception.UserAlreadyExistsException;
import ru.vovai.bankserver.exception.UserNotFoundException;
import ru.vovai.bankserver.model.Client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;


public class ClientDAOTest {

    private static PostgreSQLContainer<?> postgresContainer;

    private static final String TEST_DB_NAME = "test";
    private static final String TEST_DB_USERNAME = "test";
    private static final String TEST_DB_PASSWORD = "test";
    private static String jdbcUrl;

    private ClientDAO clientDAO;

    @BeforeClass
    public static void setUp() {
        postgresContainer = new PostgreSQLContainer<>("postgres:latest")
                .withDatabaseName(TEST_DB_NAME)
                .withUsername(TEST_DB_USERNAME)
                .withPassword(TEST_DB_PASSWORD);
        postgresContainer.start();

        Integer postgresPort = postgresContainer.getFirstMappedPort();
        jdbcUrl = "jdbc:postgresql://localhost:" + postgresPort + "/" + TEST_DB_NAME;
    }

    @AfterClass
    public static void tearDown() {
        postgresContainer.stop();
    }

    @Before
    public void setUpBeforeMethod() {
        try {
            clientDAO = new ClientDAO(jdbcUrl, TEST_DB_USERNAME, TEST_DB_PASSWORD);
            clientDAO.createClientTable();
        } catch (DatabaseException e) {
            fail("Failed to create client table: " + e.getMessage());
        }
    }

    @After
    public void tearDownAfterMethod() throws DatabaseException {
        dropClientTable();
    }

    @Test
    public void createClientTable() {
        try {
            clientDAO = new ClientDAO(jdbcUrl, TEST_DB_USERNAME, TEST_DB_PASSWORD);
            dropClientTable(); // Предполагая, что таблица существует, удаляем ее
            clientDAO.createClientTable(); // Создаем новую таблицу
            // Проверяем, что таблица создана успешно
            assertTrue(true);
        } catch (DatabaseException e) {
            fail("Failed to create or drop client table: " + e.getMessage());
        }
    }


    @Test
    public void testCreateUserAndGetBalance() {
        String login = "testuser";
        String password = "testpassword";
        try {
            clientDAO.createUser(login, password);
            int balance = clientDAO.getBalance(login);
            assertEquals(1000, balance);
        } catch (DatabaseException | UserNotFoundException | UserAlreadyExistsException e) {
            fail("Failed to create user or get balance: " + e.getMessage());
        }
    }

    @Test
    public void testTransferFunds() {
        String from = "sender";
        String to = "recipient";
        int amount = 500;
        try {
            clientDAO = new ClientDAO(jdbcUrl, TEST_DB_USERNAME, TEST_DB_PASSWORD);
            clientDAO.createClientTable();
            clientDAO.createUser(from, "password1");
            clientDAO.createUser(to, "password2");
            clientDAO.transfer(from, to, amount);
            int senderBalance = clientDAO.getBalance(from);
            int recipientBalance = clientDAO.getBalance(to);
            assertEquals(500, senderBalance);
            assertEquals(1500, recipientBalance);
        } catch (DatabaseException | UserNotFoundException | InsufficientFundsException | UserAlreadyExistsException e) {
            System.out.println(e.getMessage());
            fail("Failed to transfer funds: " + e.getMessage());
        }
    }

    @Test
    public void testInsufficientFundsTransfer() {
        String from = "sender";
        String to = "recipient";
        int transferAmount = 1500;  // Попытка перевода больше, чем у отправителя

        try {
            clientDAO = new ClientDAO(jdbcUrl, TEST_DB_USERNAME, TEST_DB_PASSWORD);
            clientDAO.createClientTable();
            clientDAO.createUser(from, "password1");
            clientDAO.createUser(to, "password2");
            // Пытаемся выполнить перевод
            clientDAO.transfer(from, to, transferAmount);
            // Если перевод удался, должно быть выброшено исключение
            fail("Failed to throw InsufficientFundsException");

        } catch (DatabaseException | UserNotFoundException | InsufficientFundsException | UserAlreadyExistsException e) {
            // Ожидается, что будет выброшено исключение InsufficientFundsException
            assertEquals("Insufficient funds", e.getMessage());
        }
    }


    @Test
    public void testGetUserByLogin() {
        try {
            clientDAO = new ClientDAO(jdbcUrl, TEST_DB_USERNAME, TEST_DB_PASSWORD);
            clientDAO.createClientTable();
            clientDAO.createUser("testUser", "testPassword");

            // Проверяем успешный поиск пользователя
            Client user = clientDAO.getUserByLogin("testUser");
            assertNotNull(user);
            assertEquals("testUser", user.getLogin());

            // Проверяем поиск несуществующего пользователя
            assertThrows(UserNotFoundException.class, () -> clientDAO.getUserByLogin("nonexistentUser"));
        } catch (DatabaseException | UserNotFoundException | UserAlreadyExistsException e) {
            fail("Failed to get user by login: " + e.getMessage());
        }
    }



    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, TEST_DB_USERNAME, TEST_DB_PASSWORD);
    }

    private void dropClientTable() throws DatabaseException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            String sql = "DROP TABLE IF EXISTS client";
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new DatabaseException("Error drop client table", e);
        }
    }

}