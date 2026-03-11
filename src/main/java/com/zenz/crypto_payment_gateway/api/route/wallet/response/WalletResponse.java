package com.zenz.crypto_payment_gateway.api.route.wallet.response;

import lombok.Data;

@Data
public class WalletResponse {
    private String walletId;
    private long balance;
    private String currency;
    private String walletAddress;
    private String merchantId;
}