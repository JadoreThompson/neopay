package com.zenz.crypto_payment_gateway.service;

import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.route.wallet.response.WalletResponse;
import com.zenz.crypto_payment_gateway.entity.Wallet;
import com.zenz.crypto_payment_gateway.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFound(
                        String.format("Failed to find wallet with id %s", walletId)
                ));
    }

    public Wallet getWalletByIdAndMerchantId(UUID walletId, UUID merchantId) {
        Wallet wallet = walletRepository.findByIdAndMerchantId(walletId, merchantId);
        if (wallet == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find wallet with id %s for merchant", walletId)
            );
        }
        return wallet;
    }

    public List<Wallet> getWalletsByMerchantId(UUID merchantId) {
        return walletRepository.findByMerchantId(merchantId);
    }

    public WalletResponse toResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setWalletId(wallet.getWalletId());
        response.setBalance(wallet.getBalance());
        response.setCurrency(wallet.getCurrency());
        response.setWalletAddress(wallet.getWalletAddress());
        
        return response;
    }
}