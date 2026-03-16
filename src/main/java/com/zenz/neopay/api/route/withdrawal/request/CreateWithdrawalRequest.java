package com.zenz.neopay.api.route.withdrawal.request;

import com.zenz.neopay.enums.Token;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigInteger;
import java.util.UUID;

@Data
public class CreateWithdrawalRequest {
    @NotNull
    private BigInteger amount;

    @NotNull
    private Token token;

    @NotBlank
    private String chain;

    private UUID merchantId;
}
