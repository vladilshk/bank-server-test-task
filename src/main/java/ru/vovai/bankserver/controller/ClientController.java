package ru.vovai.bankserver.controller;

import com.google.gson.Gson;
import ru.vovai.bankserver.dto.*;
import ru.vovai.bankserver.exception.*;
import ru.vovai.bankserver.http.HttpRequest;
import ru.vovai.bankserver.security.JwtProvider;
import ru.vovai.bankserver.service.ClientService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static ru.vovai.bankserver.http.HttpRequestParser.getHttpRequestFromStream;
import static ru.vovai.bankserver.security.JwtUtil.getTokenFromRequest;

public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    public void handleRequest(Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
        try {
            HttpRequest httpRequest = getHttpRequestFromStream(clientSocket.getInputStream());

            String requestLine = httpRequest.getMethod() + " " + httpRequest.getUri();
            switch (requestLine) {
                case "POST /signup" -> handleSignUp(httpRequest, out);
                case "POST /signin" -> handleSignIn(httpRequest, out);
                case "GET /money" -> handleGetBalance(httpRequest, out);
                case "POST /money" -> handleMakeMoneyTransfer(httpRequest, out);
                default -> sendErrorResponse(out, 404, "Not Found");
            }
        } catch (IOException | DatabaseException e) {
            sendErrorResponse(out, 500, "Internal Server Error");
        } catch (UserNotFoundException e) {
            sendErrorResponse(out, 404, e.getMessage());
        } catch (InsufficientFundsException e) {
            sendErrorResponse(out, 403, e.getMessage());
        } catch (TokenNotFoundException | TokenValidationException e) {
            sendErrorResponse(out, 401, e.getMessage());
        } catch (UserAlreadyExistsException e) {
            sendErrorResponse(out, 409, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendErrorResponse(out, 400, e.getMessage());
        }
        finally {
            out.close();
            clientSocket.close();
        }
    }


    private void handleSignUp(HttpRequest request, PrintWriter out) throws IOException, DatabaseException, UserAlreadyExistsException, IllegalArgumentException {
        ClientRegistrationRequest userDTO = convertJsonToObject(request.getBody(), ClientRegistrationRequest.class);
        clientService.createUser(userDTO);
        sendSuccessResponse(out, 201, new Gson()
                .toJson(new SingUpResponse("Registration is successful")));
    }

    private void handleSignIn(HttpRequest request, PrintWriter out) throws IOException, UserNotFoundException, DatabaseException, IllegalArgumentException {
        ClientRegistrationRequest userDTO = convertJsonToObject(request.getBody(), ClientRegistrationRequest.class);
        clientService.authenticate(userDTO);

        String login = userDTO.getLogin();
        String token = JwtProvider.generateToken(login);
        sendSuccessResponse(out, 200, new Gson()
                .toJson(new AuthenticationResponse(token)));
    }

    private void handleGetBalance(HttpRequest request, PrintWriter out) throws IOException, UserNotFoundException, DatabaseException, TokenValidationException, TokenNotFoundException, IllegalArgumentException {
        String token = getTokenFromRequest(request);
        JwtProvider.validateToken(token);

        String login = JwtProvider.getUsernameFromToken(token);
        sendSuccessResponse(out, 200, new Gson()
                .toJson(new BalanceResponse(clientService.getBalance(login))));

    }

    private void handleMakeMoneyTransfer(HttpRequest request, PrintWriter out) throws IOException, UserNotFoundException, InsufficientFundsException, DatabaseException, TokenNotFoundException, TokenValidationException {
        MoneyTransferRequest transferRequest = convertJsonToObject(request.getBody(), MoneyTransferRequest.class);
        String token = getTokenFromRequest(request);
        JwtProvider.validateToken(token);

        String login = JwtProvider.getUsernameFromToken(token);
        clientService.transferMoney(login, transferRequest.getTo(), transferRequest.getAmount());
        sendSuccessResponse(out, 200, new Gson()
                .toJson(new MoneyTransferResponse("Transaction complete!")));
    }

    private <T> T convertJsonToObject(String body, Class<T> clazz) {
        return new Gson().fromJson(body, clazz);
    }

    private void sendSuccessResponse(PrintWriter out, int statusCode, String message) {
        out.println("HTTP/1.1 " + statusCode + " OK");
        out.println("Content-Type: application/json");
        out.println();
        out.println(message);
    }

    private void sendErrorResponse(PrintWriter out, int statusCode, String message) {
        out.println("HTTP/1.1 " + statusCode + " Error");
        out.println();
        out.println(message);
    }
}