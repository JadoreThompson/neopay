package com.zenz.crypto_payment_gateway.api.route.price.model.request;

import com.zenz.crypto_payment_gateway.api.route.price.model.validation.ValidateIsRecurring;
import com.zenz.crypto_payment_gateway.enums.PricingType;
import com.zenz.crypto_payment_gateway.model.Recurring;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.UUID;

@Data
@ValidateIsRecurring
public class CreatePriceRequest {
    @Min(value = 1)
    private long amount;

    @NotNull
    private PricingType pricingType;

    @NotNull
    private String currency;

    @NotNull
    private UUID productId;

    private Recurring recurring;
}