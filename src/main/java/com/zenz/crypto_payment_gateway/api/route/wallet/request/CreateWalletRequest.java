package com.zenz.crypto_payment_gateway.api.route.wallet.request;

import lombok.Data;

@Data
public class CreateWalletRequest {
    private String currency;
    private String walletAddress;
    private String merchantId;
}