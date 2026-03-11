package com.zenz.crypto_payment_gateway.api.route.product.request;

import lombok.Data;

@Data
public class CreateProductRequest {
    private String name;
    private String description;
    private String image;
    private String walletAddress;
    private String merchantId;
}