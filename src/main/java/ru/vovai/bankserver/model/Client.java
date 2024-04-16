package ru.vovai.bankserver.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Client {

    private Long id;
    private String login;
    private String password;
    private Long balance;

}
