package com.zenz.crypto_payment_gateway.api.route.subscription;

import com.zenz.crypto_payment_gateway.api.route.subscription.request.CreateSubscriptionRequest;
import com.zenz.crypto_payment_gateway.api.route.subscription.response.SubscriptionResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscription/")
public class SubscriptionController {
    @PostMapping("/")
    public void createSubscription(@RequestBody CreateSubscriptionRequest body) {}

    @GetMapping("/{subscriptionId}/")
    public SubscriptionResponse getSubscription(@PathVariable String subscriptionId) {return null;}

    @GetMapping("/")
    public List<SubscriptionResponse> getSubscriptions() {return null;}

    @DeleteMapping("/{subscriptionId}/")
    public void cancelSubscription(@PathVariable String subscriptionId) {}
}