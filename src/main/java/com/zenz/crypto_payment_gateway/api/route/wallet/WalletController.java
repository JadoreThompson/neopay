package com.zenz.crypto_payment_gateway.api.route.wallet;

import com.zenz.crypto_payment_gateway.api.route.wallet.response.WalletResponse;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.entity.Wallet;
import com.zenz.crypto_payment_gateway.service.MerchantService;
import com.zenz.crypto_payment_gateway.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants/{merchantId}/wallets/")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final MerchantService merchantService;

    @GetMapping("/{walletId}/")
    public ResponseEntity<WalletResponse> getWallet(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID walletId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Wallet wallet = walletService.getWalletByIdAndMerchantId(walletId, merchantId);
        return ResponseEntity.ok(walletService.toResponse(wallet));
    }

    @GetMapping("/")
    public ResponseEntity<List<WalletResponse>> getWallets(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        List<Wallet> wallets = walletService.getWalletsByMerchantId(merchantId);
        List<WalletResponse> responses = wallets.stream()
                .map(walletService::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}