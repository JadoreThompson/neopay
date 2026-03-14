package com.zenz.crypto_payment_gateway.api.route.price;

import com.zenz.crypto_payment_gateway.api.config.SecurityConfig;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;

@WebMvcTest(PriceController.class)
@Import({SecurityConfig.class})
public class PriceControllerTest {
}
