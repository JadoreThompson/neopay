package com.zenz.crypto_payment_gateway.repository;

import com.zenz.crypto_payment_gateway.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByCustomerId(UUID customerId);

    List<Subscription> findByMerchantId(UUID merchantId);

    Subscription findByIdAndMerchantId(UUID id, UUID merchantId);

    Subscription findByIdAndCustomerId(UUID id, UUID customerId);
}