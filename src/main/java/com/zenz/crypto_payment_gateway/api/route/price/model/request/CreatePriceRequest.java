package com.zenz.crypto_payment_gateway.api.route.price.model.request;

import com.zenz.crypto_payment_gateway.enums.PricingType;
import com.zenz.crypto_payment_gateway.model.Recurring;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.UUID;

@Data
public class CreatePriceRequest {
    private long amount;

    @NotNull
    private PricingType pricingType;

    @NotNull
    private String currency;

    @NotNull
    private UUID productId;

    private Recurring recurring;

    @AssertTrue(message = "Recurring object must be provided for price object with type RECURRING")
    public boolean isRecurringPresent() {
        return !(pricingType.equals(PricingType.RECURRING) && recurring == null);
    }
}