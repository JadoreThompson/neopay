package com.zenz.crypto_payment_gateway.api.route.auth.model.request.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword {

    String message();

    int minLength();

    int minUppercase();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}