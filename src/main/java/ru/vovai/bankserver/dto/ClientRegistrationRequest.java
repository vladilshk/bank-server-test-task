package ru.vovai.bankserver.dto;

import lombok.Data;

@Data
public class ClientRegistrationRequest {
    private String login;
    private String password;
}
