package com.zenz.neopay.entity;

import com.zenz.neopay.enums.Token;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;
import java.util.UUID;

@Data
@Entity
@Table(name = "balances")
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID balanceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Token token;

    @Column(nullable = false)
    private BigInteger balance;
}