package com.zenz.crypto_payment_gateway.api.route.product;

import com.zenz.crypto_payment_gateway.api.route.product.request.CreateProductRequest;
import com.zenz.crypto_payment_gateway.api.route.product.request.UpdateProductRequest;
import com.zenz.crypto_payment_gateway.api.route.product.response.ProductResponse;
import com.zenz.crypto_payment_gateway.entity.Merchant;
import com.zenz.crypto_payment_gateway.entity.Product;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.service.MerchantService;
import com.zenz.crypto_payment_gateway.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants/{merchantId}/product/")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService  productService;
    private final MerchantService merchantService;

    @PostMapping("/")
    public ResponseEntity<?> createProduct(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @RequestBody CreateProductRequest body
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Product product = productService.createProduct(body, merchantId);
        return ResponseEntity.ok(productService.toResponse(product));
    }

    @GetMapping("/{productId}/")
    public ResponseEntity<?> getProduct(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID productId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Product product = productService.getProductsByIdAndMerchantId(productId, merchantId);
        return ResponseEntity.ok(productService.toResponse(product));
    }

    @GetMapping("/")
    public ResponseEntity<?> getProducts(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        List<Product> products = productService.getProductsByMerchantId(merchantId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{productId}/")
    public ResponseEntity<Product> updateProduct(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID productId,
            @RequestBody UpdateProductRequest body
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Product product = productService.getProductsByIdAndMerchantId(productId, merchantId);
        product = productService.updateProduct(product, body);
        return ResponseEntity.ok(product);
    }
}
