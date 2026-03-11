package com.zenz.crypto_payment_gateway.api.route.product.request;

import lombok.Data;

@Data
public class UpdateProductRequest {
    private String name;
    private String description;
    private String image;
    private String walletAddress;
}