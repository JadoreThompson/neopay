package com.zenz.neopay.api.route.withdrawal.response;

import com.zenz.neopay.enums.WithdrawalStatus;
import lombok.Data;

import java.math.BigInteger;
import java.util.UUID;

@Data
public class WithdrawalResponse {
    private UUID withdrawalId;
    private BigInteger amount;
    private String currency;
    private String chain;
    private WithdrawalStatus status;
    private long createdAt;
    private long completedAt;
}
