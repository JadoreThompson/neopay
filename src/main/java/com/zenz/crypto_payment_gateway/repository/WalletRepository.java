package com.zenz.crypto_payment_gateway.repository;

import com.zenz.crypto_payment_gateway.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    List<Wallet> findByMerchantId(UUID merchantId);

    Wallet findByIdAndMerchantId(UUID id, UUID merchantId);
}