package com.zenz.neopay.service.blockchain.watcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.neopay.entity.EthereumWatcherEvents;
import com.zenz.neopay.event.transaction.*;
import com.zenz.neopay.repository.EthereumWatcherEventsRepository;
import com.zenz.neopay.repository.InvoiceRepository;
import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EthereumWatcherService implements WatcherService {

    private final Web3j web3j;

    private final EthereumWatcherEventsRepository watcherEventsRepository;

    private final InvoiceRepository invoiceRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ethereum.contract.transaction-contract-address}")
    private String transactionContractAddress;

    private Disposable eventsSubscription;

    private static final Event TRANSACTION_EXECUTED_EVENT = new Event(
            "TransactionExecuted",
            List.of(
                    TypeReference.create(Bytes32.class, true),
                    TypeReference.create(Utf8String.class),
                    TypeReference.create(Address.class, true),
                    TypeReference.create(Address.class, true),
                    TypeReference.create(Address.class),
                    TypeReference.create(Uint256.class),
                    TypeReference.create(Uint256.class)
            )
    );

    private static final Event TRANSACTION_FAILED_EVENT = new Event(
            "TransactionFailed",
            List.of(
                    TypeReference.create(Bytes32.class, true),
                    TypeReference.create(Utf8String.class),
                    TypeReference.create(Address.class, true),
                    TypeReference.create(Address.class, true),
                    TypeReference.create(Address.class),
                    TypeReference.create(Uint256.class),
                    TypeReference.create(Utf8String.class),
                    TypeReference.create(Uint256.class)
            )
    );

    @PostConstruct
    @Override
    public void start() {
        log.info("Starting EthereumWatcherService for contract {}", transactionContractAddress);

        String transactionExecutedTopic = EventEncoder.encode(TRANSACTION_EXECUTED_EVENT);
        String transactionFailedTopic = EventEncoder.encode(TRANSACTION_FAILED_EVENT);

        EthFilter filter = new EthFilter(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST,
                transactionContractAddress
        );

        // Creates an OR on topic(0)
        filter.addOptionalTopics(transactionExecutedTopic, transactionFailedTopic);

        eventsSubscription = web3j.ethLogFlowable(filter).subscribe(
                this::handleLog,
                error -> log.error("Ethereum log subscription failed", error)
        );

        log.info("Subscription created successfully");
    }

    private void handleLog(Log logObject) {
        try {
            log.info("Received log: {}", logObject.toString());
            if (logObject.getTopics() == null || logObject.getTopics().isEmpty()) {
                log.warn("Received log with no topics. txHash={}", logObject.getTransactionHash());
                return;
            }

            String eventSignature = logObject.getTopics().get(0);

            if (EventEncoder.encode(TRANSACTION_EXECUTED_EVENT).equals(eventSignature)) {
                TransactionExecutedEvent event = buildTransactionExecutedEvent(logObject);
                handleTransactionExecuted(event, logObject);
                return;
            }

            if (EventEncoder.encode(TRANSACTION_FAILED_EVENT).equals(eventSignature)) {
                TransactionFailedEvent event = buildTransactionFailedEvent(logObject);
                handleTransactionFailed(event, logObject);
                return;
            }

            log.warn("Received unknown event signature {} txHash={}", eventSignature, logObject.getTransactionHash());
        } catch (Exception e) {
            log.error("Failed to process log. txHash={}", logObject.getTransactionHash(), e);
        }
    }

    public static TransactionExecutedEvent buildTransactionExecutedEvent(Log logObject) {
        var eventValues = Contract.staticExtractEventParameters(TRANSACTION_EXECUTED_EVENT, logObject);

        String transactionKey = ((Bytes32) eventValues.getIndexedValues().get(0)).getValue().toString();
        String sender = ((Address) eventValues.getIndexedValues().get(1)).getValue();
        String recipient = ((Address) eventValues.getIndexedValues().get(2)).getValue();

        String transactionId = ((Utf8String) eventValues.getNonIndexedValues().get(0)).getValue();
        String token = ((Address) eventValues.getNonIndexedValues().get(1)).getValue();
        BigInteger amount = ((Uint256) eventValues.getNonIndexedValues().get(2)).getValue();
        BigInteger timestamp = ((Uint256) eventValues.getNonIndexedValues().get(3)).getValue();

        return new TransactionExecutedEvent(
                transactionKey,
                UUID.fromString(transactionId),
                sender,
                recipient,
                token,
                amount,
                timestamp
        );
    }

    public static TransactionFailedEvent buildTransactionFailedEvent(Log logObject) {
        var eventValues = Contract.staticExtractEventParameters(TRANSACTION_FAILED_EVENT, logObject);

        String transactionKey = ((Bytes32) eventValues.getIndexedValues().get(0)).getValue().toString();
        String sender = ((Address) eventValues.getIndexedValues().get(1)).getValue();
        String recipient = ((Address) eventValues.getIndexedValues().get(2)).getValue();

        String transactionId = ((Utf8String) eventValues.getNonIndexedValues().get(0)).getValue();
        String token = ((Address) eventValues.getNonIndexedValues().get(1)).getValue();
        BigInteger amount = ((Uint256) eventValues.getNonIndexedValues().get(2)).getValue();
        String reason= ((Utf8String) eventValues.getNonIndexedValues().get(3)).getValue();
        BigInteger timestamp = ((Uint256) eventValues.getNonIndexedValues().get(4)).getValue();

        return new TransactionFailedEvent(
                transactionKey,
                UUID.fromString(transactionId),
                sender,
                recipient,
                token,
                amount,
                reason,
                timestamp
        );
    }

    public EthereumWatcherEvents logEvent(TransactionEvent event, Log logObject) throws JsonProcessingException {
        EthereumWatcherEvents watcherEvent = new EthereumWatcherEvents();
        watcherEvent.setEvent(objectMapper.writeValueAsString(event));
        watcherEvent.setTimestamp(event.timestamp());
        watcherEvent.setBlockHash(logObject.getBlockHash());
        watcherEvent.setBlockNumber(logObject.getBlockNumber());

        watcherEventsRepository.save(watcherEvent);
        return watcherEvent;
    }

    public void handleTransactionExecuted(TransactionExecutedEvent event, Log logObject) throws JsonProcessingException {
        logEvent(event, logObject);
        handleTransactionExecuted(event);
    }

    @Override
    public void handleTransactionExecuted(TransactionExecutedEvent event) {
        log.info("Handling transaction executed event {}", event.toString());
    }

    public void handleTransactionFailed(TransactionFailedEvent event, Log logObject) throws JsonProcessingException {
        logEvent(event, logObject);
        handleTransactionFailed(event);
    }

    @Override
    public void handleTransactionFailed(TransactionFailedEvent event) {
        log.info("Handling transaction failed event {}", event.toString());
    }

    @PreDestroy
    @Override
    public void stop() {
        if (eventsSubscription != null && !eventsSubscription.isDisposed()) {
            eventsSubscription.dispose();
        }
    }
}