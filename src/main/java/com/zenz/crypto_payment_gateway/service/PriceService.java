package com.zenz.crypto_payment_gateway.service;

import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.route.price.model.request.CreatePriceRequest;
import com.zenz.crypto_payment_gateway.api.route.price.model.request.UpdatePriceRequest;
import com.zenz.crypto_payment_gateway.api.route.price.model.response.PriceResponse;
import com.zenz.crypto_payment_gateway.entity.Price;
import com.zenz.crypto_payment_gateway.entity.Product;
import com.zenz.crypto_payment_gateway.repository.PriceRepository;
import com.zenz.crypto_payment_gateway.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final PriceRepository priceRepository;
    private final ProductRepository productRepository;

    public Price createPrice(UUID merchantId, CreatePriceRequest request) {
        Product product = productRepository.findByIdAndMerchantId(request.getProductId(), merchantId);
        if (product == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find product with id %s", request.getProductId())
            );
        }

        Price price = new Price();
        price.setAmount(request.getAmount());
        price.setPricingType(request.getPricingType());
        price.setCurrency(request.getCurrency());
        price.setProductId(product.getProductId());
        price.setProduct(product);
        price.setRecurring(request.getRecurring());

        return priceRepository.save(price);
    }

    public Price getPriceById(UUID priceId) {
        return priceRepository.findById(priceId)
                .orElseThrow(() -> new ResourceNotFound(
                        String.format("Failed to find price with id %s", priceId)
                ));
    }

    public Price getPriceByIdAndProductId(UUID priceId, UUID productId) {
        Price price = priceRepository.findByIdAndProductId(priceId, productId);
        if (price == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find price with id %s for product id %s", priceId, productId)
            );
        }
        return price;
    }

    public List<Price> getPricesByMerchantId(UUID merchantId) {
        return priceRepository.findByMerchantId(merchantId);
    }

    public List<Price> getPricesByMerchantIdAndProductId(UUID merchantId, UUID productId) {
        return priceRepository.findByMerchantIdAndProductId(merchantId, productId);
    }

    public List<Price> getPricesByProductId(UUID productId) {
        return priceRepository.findByProductId(productId);
    }

    public Price updatePrice(UUID priceId, UpdatePriceRequest request) {
        Price price = getPriceById(priceId);
        
        if (request.getMetadata() != null) {
            price.setMetadata(request.getMetadata());
        }
        
        return priceRepository.save(price);
    }

    public void deletePrice(UUID priceId) {
        Price price = getPriceById(priceId);
        priceRepository.delete(price);
    }

    public PriceResponse toResponse(Price price) {
        PriceResponse response = new PriceResponse();
        response.setPriceId(price.getPriceId());
        response.setProductId(price.getProduct().getProductId());
        response.setAmount(price.getAmount());
        response.setPricingType(price.getPricingType());
        response.setCurrency(price.getCurrency());
        response.setRecurring(price.getRecurring());
        response.setMetadata(price.getMetadata());

        return response;
    }
}