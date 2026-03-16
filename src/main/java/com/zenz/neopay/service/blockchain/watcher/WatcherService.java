package com.zenz.neopay.service.blockchain.watcher;


import com.zenz.neopay.event.transaction.TransactionExecutedEvent;
import com.zenz.neopay.event.transaction.TransactionFailedEvent;

public interface WatcherService {

    void start();

    void stop();

    void handleTransactionExecuted(TransactionExecutedEvent event);

    void handleTransactionFailed(TransactionFailedEvent event);
}
