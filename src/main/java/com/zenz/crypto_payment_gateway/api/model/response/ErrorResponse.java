package com.zenz.crypto_payment_gateway.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private ErrorDetail error;
}
