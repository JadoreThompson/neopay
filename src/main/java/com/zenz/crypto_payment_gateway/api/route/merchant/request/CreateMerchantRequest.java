package com.zenz.crypto_payment_gateway.api.route.merchant.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMerchantRequest {
    
    @NotBlank(message = "Merchant name is required")
    @Size(max = 255, message = "Merchant name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
