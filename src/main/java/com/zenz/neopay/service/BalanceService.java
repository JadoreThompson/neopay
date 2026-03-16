package com.zenz.neopay.service;

import com.zenz.neopay.entity.Balance;
import com.zenz.neopay.enums.Token;
import com.zenz.neopay.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepository;

    public Optional<Balance> findByToken(Token token) {
        return balanceRepository.findAll().stream()
                .filter(balance -> balance.getToken() == token)
                .findFirst();
    }

    public Optional<Balance> findById(UUID balanceId) {
        return balanceRepository.findById(balanceId);
    }

    public Balance save(Balance balance) {
        return balanceRepository.save(balance);
    }

    public Balance createBalance(Token token, BigInteger initialBalance) {
        Balance balance = new Balance();
        balance.setToken(token);
        balance.setBalance(initialBalance);
        return balanceRepository.save(balance);
    }

    public Balance increaseBalance(Balance balance, BigInteger amount) {
        balance.setBalance(balance.getBalance().add(amount));
        return balanceRepository.save(balance);
    }

    public Balance decreaseBalance(Balance balance, BigInteger amount) {
        balance.setBalance(balance.getBalance().subtract(amount));
        return balanceRepository.save(balance);
    }
}