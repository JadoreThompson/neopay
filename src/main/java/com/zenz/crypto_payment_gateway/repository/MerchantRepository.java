package com.zenz.crypto_payment_gateway.repository;

import com.zenz.crypto_payment_gateway.entity.Merchant;
import com.zenz.crypto_payment_gateway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    List<Merchant> findByUser(User user);
    List<Merchant> findByUserId(UUID userId);
    Optional<Merchant> findByIdAndUserId(UUID id, UUID userId);
}
