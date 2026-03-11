package com.zenz.crypto_payment_gateway.api.route.invoice;

import com.zenz.crypto_payment_gateway.api.route.invoice.request.CreateInvoiceRequest;
import com.zenz.crypto_payment_gateway.api.route.invoice.response.InvoiceResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoice/")
public class InvoiceController {
    @PostMapping("/")
    public void createInvoice(@RequestBody CreateInvoiceRequest body) {}

    @GetMapping("/{invoiceId}/")
    public InvoiceResponse getInvoice(@PathVariable String invoiceId) {return null;}

    @GetMapping("/")
    public List<InvoiceResponse> getInvoices() {return null;}

    @DeleteMapping("/{invoiceId}/")
    public void deleteInvoice(@PathVariable String invoiceId) {}
}