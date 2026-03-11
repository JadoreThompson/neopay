package com.zenz.crypto_payment_gateway.api.route.product;

import com.zenz.crypto_payment_gateway.api.route.product.request.CreateProductRequest;
import com.zenz.crypto_payment_gateway.api.route.product.request.UpdateProductRequest;
import com.zenz.crypto_payment_gateway.api.route.product.response.ProductResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product/")
public class ProductController {
    @PostMapping("/")
    public void createProduct(@RequestBody CreateProductRequest body) {}

    @GetMapping("/{productId}/")
    public ProductResponse getProduct(@PathVariable String productId) {return null;}

    @GetMapping("/")
    public List<ProductResponse> getProducts() {return null;}

    @PutMapping("/{productId}/")
    public ProductResponse updateProduct(@RequestBody UpdateProductRequest body, @PathVariable String productId) {
        return null;
    }

    @DeleteMapping("/{productId}/")
    public void deleteProduct(@PathVariable String productId) {}
}