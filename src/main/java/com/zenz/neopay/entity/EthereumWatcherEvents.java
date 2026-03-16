package com.zenz.neopay.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;
import java.util.UUID;

@Data
@Entity
@Table(name = "ethereum_watcher_data")
public class EthereumWatcherEvents {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID eventId;

    // JSON string
    private String event;

    @Column(nullable = false, updatable = false)
    private String blockHash;

    @Column(nullable = false, updatable = false)
    private BigInteger blockNumber;

    @Column(nullable = false, updatable = false)
    private BigInteger timestamp;
}
