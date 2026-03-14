package com.zenz.crypto_payment_gateway.service;

import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.route.product.request.CreateProductRequest;
import com.zenz.crypto_payment_gateway.api.route.product.request.UpdateProductRequest;
import com.zenz.crypto_payment_gateway.api.route.product.response.ProductResponse;
import com.zenz.crypto_payment_gateway.entity.Product;
import com.zenz.crypto_payment_gateway.repository.MerchantRepository;
import com.zenz.crypto_payment_gateway.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private ProductRepository productRepository;
    private MerchantRepository merchantRepository;

//    public Product createProduct(Product product) {
//        return productRepository.save(product);
//    }

    public Product createProduct(CreateProductRequest request, UUID merchantId) {
        Product product = new Product();
        product.setMerchantId(merchantId);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImage(request.getImage());

        productRepository.save(product);
        return product;
    }

    public Product getProductById(UUID id) {
        Product product  = productRepository.findById(id).orElse(null);
        if (product == null) {
            throw new ResourceNotFound("Product not found");
        }

        return product;
    }

    public List<Product> getProductsByMerchantId(UUID merchantId) {
        return productRepository.findByMerchantId(merchantId);
    }

    public Product getProductsByIdAndMerchantId(UUID id, UUID merchantId) {
        Product product = productRepository.findByIdAndMerchantId(id, merchantId);
        if (product == null) {
            throw new ResourceNotFound(
                    String.format("Failed to find product with id %s for merchant id %s", id, merchantId)
            );
        }
        return product;
    }

    public Product updateProduct(Product product, UpdateProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImage(request.getImage());

        productRepository.save(product);
        return product;
    }

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();

        response.setProductId(product.getProductId());
        response.setMerchantId(product.getMerchantId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setImage(product.getImage());
        response.setCreatedAt(product.getCreatedAt());

        return response;
    }
}
