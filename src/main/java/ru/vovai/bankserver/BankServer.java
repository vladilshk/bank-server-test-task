package ru.vovai.bankserver;

import ru.vovai.bankserver.controller.ClientController;
import ru.vovai.bankserver.dao.ClientDAO;
import ru.vovai.bankserver.exception.DatabaseException;
import ru.vovai.bankserver.service.ClientService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import static ru.vovai.bankserver.config.DatabaseConfig.*;

public class BankServer {

    private final int port;
    private final ClientController clientController;

    public BankServer(int port,  ClientController clientController) {
        this.port = port;
        this.clientController = clientController;
    }

    public static void main(String[] args) {
        try {
            initializeDatabase();
            startServer(8080);
        } catch (DatabaseException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            // Создаем отдельный поток для чтения команд с консоли
            Thread consoleThread = new Thread(this::readConsoleInput);
            consoleThread.start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Обработка каждого клиента в отдельном потоке
                Thread clientThread = new Thread(() -> handleRequest(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleRequest(Socket clientSocket) {
        try {
            clientController.handleRequest(clientSocket);
        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void readConsoleInput() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String input = reader.readLine();
                if ("exit".equalsIgnoreCase(input.trim())) {
                    System.out.println("Exiting server...");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading console input: " + e.getMessage());
        }
    }

    private static void initializeDatabase() throws DatabaseException {
        ClientDAO clientDAO = new ClientDAO(DB_URL, DB_USERNAME, DB_PASSWORD);
        clientDAO.createClientTable();
    }

    private static void startServer(int port) {
        ClientService clientService = new ClientService(new ClientDAO(DB_URL, DB_USERNAME, DB_PASSWORD));
        ClientController clientController = new ClientController(clientService);
        BankServer server = new BankServer(port, clientController);
        server.start();
    }
}

