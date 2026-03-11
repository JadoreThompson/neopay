package com.zenz.crypto_payment_gateway.api.route.customer.request;

import lombok.Data;

@Data
public class CreateCustomerRequest {
    private String nickname;
    private String email;
    private String merchantId;
}