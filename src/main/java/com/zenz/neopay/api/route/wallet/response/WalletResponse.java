package com.zenz.neopay.api.route.wallet.response;

import lombok.Data;

import java.math.BigInteger;
import java.util.UUID;

@Data
public class WalletResponse {
    private UUID walletId;
    private BigInteger balance;
    private BigInteger escrow;
    private String token;
    private String walletAddress;
}
