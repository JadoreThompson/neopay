package com.zenz.crypto_payment_gateway.api.route.product.response;

import lombok.Data;

@Data
public class ProductResponse {
    private String productId;
    private String name;
    private String description;
    private String image;
    private String walletAddress;
    private String merchantId;
    private long createdAt;
}