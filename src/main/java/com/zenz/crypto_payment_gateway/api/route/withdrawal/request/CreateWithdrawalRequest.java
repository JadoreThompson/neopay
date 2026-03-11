package com.zenz.crypto_payment_gateway.api.route.withdrawal.request;

import lombok.Data;

@Data
public class CreateWithdrawalRequest {
    private long amount;
    private String currency;
    private String chain;
    private String merchantId;
}