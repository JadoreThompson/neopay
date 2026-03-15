package com.zenz.crypto_payment_gateway.service.blockchain.watcher;


import com.zenz.crypto_payment_gateway.service.blockchain.event.InvoiceCreatedEvent;
import com.zenz.crypto_payment_gateway.service.blockchain.event.InvoiceExecutedEvent;

public interface WatcherService {

    void start();

    void stop();

    void handleInvoiceCreated(InvoiceCreatedEvent event);

    void handleInvoiceExecuted(InvoiceExecutedEvent event);
}
