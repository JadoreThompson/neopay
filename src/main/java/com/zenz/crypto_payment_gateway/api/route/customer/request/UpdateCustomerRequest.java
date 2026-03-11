package com.zenz.crypto_payment_gateway.api.route.customer.request;

import lombok.Data;

@Data
public class UpdateCustomerRequest {
    private String nickname;
    private String email;
}