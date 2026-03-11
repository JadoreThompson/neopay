package com.zenz.crypto_payment_gateway.api.route.customer;

import com.zenz.crypto_payment_gateway.api.route.customer.request.CreateCustomerRequest;
import com.zenz.crypto_payment_gateway.api.route.customer.request.UpdateCustomerRequest;
import com.zenz.crypto_payment_gateway.api.route.customer.response.CustomerResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/")
public class CustomerController {
    @PostMapping("/")
    public void createCustomer(@RequestBody CreateCustomerRequest body) {}

    @GetMapping("/{customerId}/")
    public CustomerResponse getCustomer(@PathVariable String customerId) {return null;}

    @GetMapping("/")
    public List<CustomerResponse> getCustomers() {return null;}

    @PutMapping("/{customerId}/")
    public CustomerResponse updateCustomer(@RequestBody UpdateCustomerRequest body, @PathVariable String customerId) {
        return null;
    }

    @DeleteMapping("/{customerId}/")
    public void deleteCustomer(@PathVariable String customerId) {}
}