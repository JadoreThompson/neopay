package com.zenz.crypto_payment_gateway.api.route.withdrawal;

import com.zenz.crypto_payment_gateway.api.route.withdrawal.request.CreateWithdrawalRequest;
import com.zenz.crypto_payment_gateway.api.route.withdrawal.response.WithdrawalResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/withdrawal/")
public class WithdrawalController {
    @PostMapping("/")
    public void createWithdrawal(@RequestBody CreateWithdrawalRequest body) {}

    @GetMapping("/{withdrawalId}/")
    public WithdrawalResponse getWithdrawal(@PathVariable String withdrawalId) {return null;}

    @GetMapping("/")
    public List<WithdrawalResponse> getWithdrawals() {return null;}

    @GetMapping("/merchant/{merchantId}/")
    public List<WithdrawalResponse> getWithdrawalsByMerchant(@PathVariable String merchantId) {return null;}
}