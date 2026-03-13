package com.zenz.crypto_payment_gateway.api.route.subscription;

import com.zenz.crypto_payment_gateway.api.route.subscription.request.CreateSubscriptionRequest;
import com.zenz.crypto_payment_gateway.api.route.subscription.response.SubscriptionResponse;
import com.zenz.crypto_payment_gateway.entity.Subscription;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.service.MerchantService;
import com.zenz.crypto_payment_gateway.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants/{merchantId}/subscription/")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final MerchantService merchantService;

    @PostMapping("/")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @Valid @RequestBody CreateSubscriptionRequest body
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Subscription subscription = subscriptionService.createSubscription(merchantId, body);
        return ResponseEntity.ok(subscriptionService.toResponse(subscription));
    }

    @GetMapping("/{subscriptionId}/")
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID subscriptionId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Subscription subscription = subscriptionService.getSubscriptionByIdAndMerchantId(subscriptionId, merchantId);
        return ResponseEntity.ok(subscriptionService.toResponse(subscription));
    }

    @GetMapping("/")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        List<Subscription> subscriptions = subscriptionService.getSubscriptionsByMerchantId(merchantId);
        List<SubscriptionResponse> responses = subscriptions.stream()
                .map(subscriptionService::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{subscriptionId}/")
    public ResponseEntity<Void> cancelSubscription(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID subscriptionId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        subscriptionService.cancelSubscription(subscriptionId);
        return ResponseEntity.noContent().build();
    }
}