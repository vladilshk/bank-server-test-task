package ru.vovai.bankserver.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import ru.vovai.bankserver.dao.ClientDAO;
import ru.vovai.bankserver.dto.BalanceResponse;
import ru.vovai.bankserver.dto.ClientRegistrationRequest;
import ru.vovai.bankserver.exception.DatabaseException;
import ru.vovai.bankserver.exception.InsufficientFundsException;
import ru.vovai.bankserver.exception.UserAlreadyExistsException;
import ru.vovai.bankserver.exception.UserNotFoundException;
import ru.vovai.bankserver.model.Client;
import ru.vovai.bankserver.security.PasswordEncoder;

import static ru.vovai.bankserver.security.PasswordEncoder.hashPassword;

public class ClientService {

    private final Logger logger;
    private final ClientDAO clientDAO;

    public ClientService(ClientDAO clientDAO) {
        this.clientDAO = clientDAO;

        String log4jConfigFile = "log4j2.xml";
        Configurator.initialize(null, log4jConfigFile);
        this.logger = LogManager.getLogger(ClientService.class);
    }

    public void createUser(ClientRegistrationRequest request) throws DatabaseException, UserAlreadyExistsException, IllegalArgumentException {
        try {
            if (request.getLogin() == null || request.getLogin().equals(" ") || request.getPassword() == null || request.getPassword().isEmpty()){
                throw new IllegalArgumentException("Name and password must not be null or empty");
            }
            String hashedPassword = hashPassword(request.getPassword());
            clientDAO.createUser(request.getLogin(), hashedPassword);
            logger.info("User '{}' was created successfully", request.getLogin());
        } catch (DatabaseException e) {
            logger.error("Failed to create user '{}'", request.getLogin(), e);
            throw e;
        }
    }

    public void authenticate(ClientRegistrationRequest request) throws UserNotFoundException, DatabaseException, IllegalArgumentException {
        try {
            if (request.getLogin() == null || request.getLogin().equals(" ") || request.getPassword() == null || request.getPassword().isEmpty()){
                throw new IllegalArgumentException("Name and password must not be null or empty");
            }
            Client user = clientDAO.getUserByLogin(request.getLogin());
            PasswordEncoder.checkPassword(request.getPassword(), user.getPassword());
            logger.info("User '{}' authentication successful", request.getLogin());
        } catch (UserNotFoundException | DatabaseException e) {
            logger.error("Authentication failed for user '{}'", request.getLogin(), e);
            throw e;
        }
    }

    public int getBalance(String name) throws UserNotFoundException, DatabaseException {
        try {
            int balance = clientDAO.getBalance(name);
            logger.info("Balance for user '{}' is {}", name, balance);
            return balance;
        } catch (UserNotFoundException | DatabaseException e) {
            logger.error("Failed to get balance for user '{}'", name, e);
            throw e;
        }
    }

    public synchronized void transferMoney(String from, String to, int amount) throws UserNotFoundException, InsufficientFundsException, DatabaseException {
        try {
            if (to == null || to.isEmpty() || amount < 1) {
                throw new IllegalArgumentException("Invalid 'to' or 'amount' value");
            }
            if (from.equals(to)){
                throw new IllegalArgumentException("You can't transfer money to yourself");
            }
            clientDAO.transfer(from, to, amount);
            logger.info("Transfer of {} from user '{}' to user '{}' was successful", amount, from, to);
        } catch (UserNotFoundException | InsufficientFundsException | DatabaseException e) {
            logger.error("Failed to transfer {} from user '{}' to user '{}'", amount, from, to, e);
            throw e;
        }
    }

}
