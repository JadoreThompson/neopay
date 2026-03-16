package com.zenz.neopay.service;

import com.zenz.neopay.api.error.ResourceNotFound;
import com.zenz.neopay.api.route.wallet.response.WalletResponse;
import com.zenz.neopay.entity.Wallet;
import com.zenz.neopay.repository.WalletRepository;
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
        Wallet wallet = walletRepository.findByWalletIdAndMerchantId(walletId, merchantId);
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

    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    public WalletResponse toResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setWalletId(wallet.getWalletId());
        response.setBalance(wallet.getBalance());
        response.setEscrow(wallet.getEscrow());
        response.setToken(wallet.getToken());
        response.setWalletAddress(wallet.getWalletAddress());
        
        return response;
    }
}