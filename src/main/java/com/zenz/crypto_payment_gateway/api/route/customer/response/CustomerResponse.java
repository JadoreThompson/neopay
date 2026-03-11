package com.zenz.crypto_payment_gateway.api.route.customer.response;

import lombok.Data;

@Data
public class CustomerResponse {
    private String customerId;
    private String nickname;
    private String email;
    private String merchantId;
    private long createdAt;
}