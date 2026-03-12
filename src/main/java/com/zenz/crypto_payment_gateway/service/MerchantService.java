package com.zenz.crypto_payment_gateway.service;

import com.zenz.crypto_payment_gateway.api.route.merchant.request.CreateMerchantRequest;
import com.zenz.crypto_payment_gateway.api.route.merchant.request.UpdateMerchantRequest;
import com.zenz.crypto_payment_gateway.api.route.merchant.response.MerchantResponse;
import com.zenz.crypto_payment_gateway.entity.Merchant;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {
    
    private final MerchantRepository merchantRepository;
    
    public Merchant createMerchant(User user, CreateMerchantRequest request) {
        Merchant merchant = new Merchant();
        merchant.setName(request.getName());
        merchant.setDescription(request.getDescription());
        merchant.setUser(user);
        
        return merchantRepository.save(merchant);
    }
    
    public Merchant getMerchantById(UUID merchantId) {
        return merchantRepository.findById(merchantId).orElse(null);
    }
    
    public Merchant getMerchantByIdAndUserId(UUID merchantId, UUID userId) {
        return merchantRepository.findByMerchantIdAndUserId(merchantId, userId).orElse(null);
    }
    
    public List<Merchant> getMerchantsByUser(User user) {
        return merchantRepository.findByUser(user);
    }
    
    public List<Merchant> getMerchantsByUserId(UUID userId) {
        return merchantRepository.findByUserId(userId);
    }
    
    public Merchant updateMerchant(UUID merchantId, UpdateMerchantRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId).orElse(null);
        
        if (merchant == null) {
            return null;
        }
        
        if (request.getName() != null) {
            merchant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            merchant.setDescription(request.getDescription());
        }
        
        return merchantRepository.save(merchant);
    }
    
    public MerchantResponse toResponse(Merchant merchant) {
        MerchantResponse response = new MerchantResponse();
        response.setMerchantId(merchant.getMerchantId());
        response.setName(merchant.getName());
        response.setDescription(merchant.getDescription());
        response.setCreatedAt(merchant.getCreatedAt());
        return response;
    }
}