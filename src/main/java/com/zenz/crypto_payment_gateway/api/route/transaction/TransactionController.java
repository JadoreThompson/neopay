package com.zenz.crypto_payment_gateway.api.route.transaction;

import com.zenz.crypto_payment_gateway.api.route.transaction.response.TransactionResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction/")
public class TransactionController {
    @GetMapping("/{transactionId}/")
    public TransactionResponse getTransaction(@PathVariable String transactionId) {return null;}

    @GetMapping("/")
    public List<TransactionResponse> getTransactions() {return null;}

    @GetMapping("/invoice/{invoiceId}/")
    public List<TransactionResponse> getTransactionsByInvoice(@PathVariable String invoiceId) {return null;}
}