package com.zenz.crypto_payment_gateway.api.route.wallet;

import com.zenz.crypto_payment_gateway.api.route.wallet.request.CreateWalletRequest;
import com.zenz.crypto_payment_gateway.api.route.wallet.response.WalletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallet/")
public class WalletController {
    @PostMapping("/")
    public void createWallet(@RequestBody CreateWalletRequest body) {}

    @GetMapping("/{walletId}/")
    public WalletResponse getWallet(@PathVariable String walletId) {return null;}

    @GetMapping("/")
    public List<WalletResponse> getWallets() {return null;}

    @GetMapping("/merchant/{merchantId}/")
    public List<WalletResponse> getWalletsByMerchant(@PathVariable String merchantId) {return null;}

    @DeleteMapping("/{walletId}/")
    public void deleteWallet(@PathVariable String walletId) {}
}