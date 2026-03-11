package com.zenz.crypto_payment_gateway.api.route.withdrawal.response;

import com.zenz.crypto_payment_gateway.enums.WithdrawalStatus;
import lombok.Data;

@Data
public class WithdrawalResponse {
    private String withdrawalId;
    private long amount;
    private String currency;
    private String chain;
    private WithdrawalStatus status;
    private String merchantId;
    private long createdAt;
    private long completedAt;
}