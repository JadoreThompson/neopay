package com.zenz.crypto_payment_gateway.api.route.price;

import com.zenz.crypto_payment_gateway.api.route.price.request.CreatePriceRequest;
import com.zenz.crypto_payment_gateway.api.route.price.request.UpdatePriceRequest;
import com.zenz.crypto_payment_gateway.api.route.price.response.PriceResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/price/")
public class PriceController {
    @PostMapping("/")
    public void createPrice(@RequestBody CreatePriceRequest body) {}

    @GetMapping("/{priceId}/")
    public PriceResponse getPrice(@PathVariable String priceId) {return null;}

    @GetMapping("/")
    public List<PriceResponse> getPrices() {return null;}

    @PutMapping("/{priceId}/")
    public PriceResponse updatePrice(@RequestBody UpdatePriceRequest body, @PathVariable String priceId) {
        return null;
    }

    @DeleteMapping("/{priceId}/")
    public void deletePrice(@PathVariable String priceId) {}
}