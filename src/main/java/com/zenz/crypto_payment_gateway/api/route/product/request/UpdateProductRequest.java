package com.zenz.crypto_payment_gateway.api.route.product.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProductRequest {
    @Size(min = 1, max = 255)
    private String name;

    @Size(min = 1, max = 255)
    private String description;

    private String image;
}