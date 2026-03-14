package com.zenz.crypto_payment_gateway.api.route.price.model.validation;

import com.zenz.crypto_payment_gateway.api.route.price.model.request.CreatePriceRequest;
import com.zenz.crypto_payment_gateway.enums.PricingType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsRecurringValidator implements ConstraintValidator<ValidateIsRecurring, CreatePriceRequest> {

    @Override
    public boolean isValid(CreatePriceRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (request.getPricingType() == PricingType.RECURRING && request.getRecurring() == null) {
            context.buildConstraintViolationWithTemplate(
                            "Recurring must be present when pricingType is RECURRING"
                    )
                    .addPropertyNode("recurring")
                    .addConstraintViolation();

            return false;
        }

        if (request.getPricingType() == PricingType.ONE_TIME && request.getRecurring() != null) {
            context.buildConstraintViolationWithTemplate(
                            "Recurring must be null when pricingType is ONE_TIME"
                    )
                    .addPropertyNode("recurring")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }

}
