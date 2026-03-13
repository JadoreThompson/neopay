package com.zenz.crypto_payment_gateway.api.route.withdrawal;

import com.zenz.crypto_payment_gateway.api.route.withdrawal.request.CreateWithdrawalRequest;
import com.zenz.crypto_payment_gateway.api.route.withdrawal.response.WithdrawalResponse;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.entity.Withdrawal;
import com.zenz.crypto_payment_gateway.service.MerchantService;
import com.zenz.crypto_payment_gateway.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants/{merchantId}/withdrawals/")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;
    private final MerchantService merchantService;

    @PostMapping("/")
    public ResponseEntity<WithdrawalResponse> createWithdrawal(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @Valid @RequestBody CreateWithdrawalRequest body
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Withdrawal withdrawal = withdrawalService.createWithdrawal(merchantId, body);
        return ResponseEntity.ok(withdrawalService.toResponse(withdrawal));
    }

    @GetMapping("/{withdrawalId}/")
    public ResponseEntity<WithdrawalResponse> getWithdrawal(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID withdrawalId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Withdrawal withdrawal = withdrawalService.getWithdrawalByIdAndMerchantId(withdrawalId, merchantId);
        return ResponseEntity.ok(withdrawalService.toResponse(withdrawal));
    }

    @GetMapping("/")
    public ResponseEntity<List<WithdrawalResponse>> getWithdrawals(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        List<Withdrawal> withdrawals = withdrawalService.getWithdrawalsByMerchantId(merchantId);
        List<WithdrawalResponse> responses = withdrawals.stream()
                .map(withdrawalService::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}