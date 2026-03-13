package com.zenz.crypto_payment_gateway.service;

import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.route.subscription.request.CreateSubscriptionRequest;
import com.zenz.crypto_payment_gateway.api.route.subscription.response.SubscriptionResponse;
import com.zenz.crypto_payment_gateway.entity.Customer;
import com.zenz.crypto_payment_gateway.entity.Price;
import com.zenz.crypto_payment_gateway.entity.Product;
import com.zenz.crypto_payment_gateway.entity.Subscription;
import com.zenz.crypto_payment_gateway.enums.SubscriptionStatus;
import com.zenz.crypto_payment_gateway.repository.CustomerRepository;
import com.zenz.crypto_payment_gateway.repository.PriceRepository;
import com.zenz.crypto_payment_gateway.repository.ProductRepository;
import com.zenz.crypto_payment_gateway.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;

    public Subscription createSubscription(UUID merchantId, CreateSubscriptionRequest request) {
        Customer customer = customerRepository.findByIdAndMerchantId(request.getCustomerId(), merchantId);
        if (customer == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find customer with id %s for merchant", request.getCustomerId())
            );
        }

        Product product = productRepository.findByIdAndMerchantId(request.getProductId(), merchantId);
        if (product == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find product with id %s for merchant", request.getProductId())
            );
        }

        Price price = priceRepository.findByIdAndProductId(request.getPriceId(), request.getProductId());
        if (price == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find price with id %s for product", request.getPriceId())
            );
        }

        Subscription subscription = new Subscription();
        subscription.setCustomerId(request.getCustomerId());
        subscription.setProductId(request.getProductId());
        subscription.setPriceId(request.getPriceId());
        subscription.setQuantity(request.getQuantity());
        subscription.setStatus(SubscriptionStatus.UNPAID);
        subscription.setStartedAt(System.currentTimeMillis());

        return subscriptionRepository.save(subscription);
    }

    public Subscription getSubscriptionById(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFound(
                        String.format("Failed to find subscription with id %s", subscriptionId)
                ));
    }

    public Subscription getSubscriptionByIdAndMerchantId(UUID subscriptionId, UUID merchantId) {
        Subscription subscription = subscriptionRepository.findByIdAndMerchantId(subscriptionId, merchantId);
        if (subscription == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find subscription with id %s for merchant", subscriptionId)
            );
        }
        return subscription;
    }

    public List<Subscription> getSubscriptionsByMerchantId(UUID merchantId) {
        return subscriptionRepository.findByMerchantId(merchantId);
    }

    public List<Subscription> getSubscriptionsByCustomerId(UUID customerId) {
        return subscriptionRepository.findByCustomerId(customerId);
    }

    public void cancelSubscription(UUID subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);
    }

    public SubscriptionResponse toResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setSubscriptionId(subscription.getSubscriptionId());
        response.setQuantity(subscription.getQuantity());
        response.setStatus(subscription.getStatus());
        response.setCustomerId(subscription.getCustomerId());
        response.setProductId(subscription.getProductId());
        response.setPriceId(subscription.getPriceId());
        response.setStartedAt(subscription.getStartedAt());
        response.setCreatedAt(subscription.getCreatedAt());
        
        return response;
    }
}