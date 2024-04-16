package ru.vovai.bankserver.dto;

import lombok.Data;

@Data
public class MoneyTransferRequest {
    private String to;
    private int amount;
}
