package com.zenz.crypto_payment_gateway.api.route.merchant;

import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.route.merchant.request.CreateMerchantRequest;
import com.zenz.crypto_payment_gateway.api.route.merchant.request.UpdateMerchantRequest;
import com.zenz.crypto_payment_gateway.api.route.merchant.response.MerchantResponse;
import com.zenz.crypto_payment_gateway.entity.Merchant;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants/")
@RequiredArgsConstructor
public class MerchantController {
    
    private final MerchantService merchantService;

    @PostMapping
    public ResponseEntity<MerchantResponse> createMerchant(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateMerchantRequest body) {
        
        Merchant merchant = merchantService.createMerchant(user, body);
        return ResponseEntity.ok(merchantService.toResponse(merchant));
    }

    @GetMapping("/{merchantId}/")
    public ResponseEntity<MerchantResponse> getMerchant(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId
    ) {
        Merchant merchant = merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        
        if (merchant == null) {
            throw new ResourceNotFound("Merchant not found");
        }
        
        return ResponseEntity.ok(merchantService.toResponse(merchant));
    }

    @GetMapping("/")
    public ResponseEntity<List<MerchantResponse>> getMerchants(@AuthenticationPrincipal User user) {
        List<Merchant> merchants = merchantService.getMerchantsByUser(user);
        
        List<MerchantResponse> responses = merchants.stream()
                .map(merchantService::toResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{merchantId}/")
    public ResponseEntity<MerchantResponse> updateMerchant(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @Valid @RequestBody UpdateMerchantRequest body
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Merchant merchant = merchantService.updateMerchant(merchantId, body);
        return ResponseEntity.ok(merchantService.toResponse(merchant));
    }
}