package com.zenz.crypto_payment_gateway.api.route.invoice;

import com.zenz.crypto_payment_gateway.api.route.invoice.request.CreateInvoiceRequest;
import com.zenz.crypto_payment_gateway.api.route.invoice.response.InvoiceResponse;
import com.zenz.crypto_payment_gateway.entity.Invoice;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.service.InvoiceService;
import com.zenz.crypto_payment_gateway.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants/{merchantId}/invoice/")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final MerchantService merchantService;

    @PostMapping("/")
    public ResponseEntity<InvoiceResponse> createInvoice(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @Valid @RequestBody CreateInvoiceRequest body
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Invoice invoice = invoiceService.createInvoice(merchantId, body);
        return ResponseEntity.ok(invoiceService.toResponse(invoice));
    }

    @GetMapping("/{invoiceId}/")
    public ResponseEntity<InvoiceResponse> getInvoice(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID invoiceId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        Invoice invoice = invoiceService.getInvoiceByIdAndMerchantId(invoiceId, merchantId);
        return ResponseEntity.ok(invoiceService.toResponse(invoice));
    }

    @GetMapping("/")
    public ResponseEntity<List<InvoiceResponse>> getInvoices(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        List<Invoice> invoices = invoiceService.getInvoicesByMerchantId(merchantId);
        List<InvoiceResponse> responses = invoices.stream()
                .map(invoiceService::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{invoiceId}/")
    public ResponseEntity<Void> deleteInvoice(
            @AuthenticationPrincipal User user,
            @PathVariable UUID merchantId,
            @PathVariable UUID invoiceId
    ) {
        merchantService.getMerchantByIdAndUserId(merchantId, user.getUserId());
        invoiceService.deleteInvoice(invoiceId);
        return ResponseEntity.noContent().build();
    }
}