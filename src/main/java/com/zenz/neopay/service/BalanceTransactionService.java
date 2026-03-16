package com.zenz.neopay.service;

import com.zenz.neopay.entity.Balance;
import com.zenz.neopay.entity.Wallet;
import com.zenz.neopay.enums.Token;
import com.zenz.neopay.event.balance.DecreaseBalance;
import com.zenz.neopay.event.balance.IncreaseBalance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceTransactionService {

    private final BalanceService balanceService;
    private final WalletService walletService;

    @Transactional
    public void processIncreaseBalance(IncreaseBalance event) {
        log.info("Processing balance increase for merchant {} transaction {} amount {} token {}",
                event.merchantId(), event.transactionId(), event.amount(), event.token());

        // Update Wallet
        Wallet wallet = walletService.getWalletsByMerchantId(event.merchantId()).stream()
                .filter(w -> w.getToken().equals(event.token().getValue()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Wallet not found for merchant %s and token %s", event.merchantId(), event.token())
                ));

        wallet.setBalance(wallet.getBalance().add(event.amount()));
        walletService.save(wallet);

        // Update Balance
        Balance balance = balanceService.findByToken(event.token())
                .orElseGet(() -> balanceService.createBalance(event.token(), BigInteger.ZERO));

        balanceService.increaseBalance(balance, event.amount());

        log.info("Successfully processed balance increase for merchant {} transaction {}", 
                event.merchantId(), event.transactionId());
    }

    @Transactional
    public void processDecreaseBalance(DecreaseBalance event) {
        log.info("Processing balance decrease for merchant {} transaction {} amount {} token {}",
                event.merchantId(), event.transactionId(), event.amount(), event.token());

        // Update Wallet
        Wallet wallet = walletService.getWalletsByMerchantId(event.merchantId()).stream()
                .filter(w -> w.getToken().equals(event.token().getValue()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Wallet not found for merchant %s and token %s", event.merchantId(), event.token())
                ));

        wallet.setBalance(wallet.getBalance().subtract(event.amount()));
        walletService.save(wallet);

        // Update Balance
        Balance balance = balanceService.findByToken(event.token())
                .orElseThrow(() -> new RuntimeException(
                        String.format("Balance not found for token %s", event.token())
                ));

        balanceService.decreaseBalance(balance, event.amount());

        log.info("Successfully processed balance decrease for merchant {} transaction {}",
                event.merchantId(), event.transactionId());
    }

    @Transactional
    public void increaseBalance(UUID merchantId, UUID transactionId, BigInteger amount, Token token) {
        IncreaseBalance event = new IncreaseBalance(transactionId, merchantId, amount, token);
        processIncreaseBalance(event);
    }

    @Transactional
    public void decreaseBalance(UUID merchantId, UUID transactionId, BigInteger amount, Token token) {
        DecreaseBalance event = new DecreaseBalance(transactionId, merchantId, amount, token);
        processDecreaseBalance(event);
    }

    @Transactional
    public void initiateWithdrawal(UUID merchantId, UUID withdrawalId, BigInteger amount, Token token) {
        log.info("Initiating withdrawal for merchant {} withdrawal {} amount {} token {}",
                merchantId, withdrawalId, amount, token);

        // Find wallet for merchant and token
        Wallet wallet = walletService.getWalletsByMerchantId(merchantId).stream()
                .filter(w -> w.getToken().equals(token.getValue()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Wallet not found for merchant %s and token %s", merchantId, token)
                ));

        // Check sufficient balance
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                    String.format("Insufficient balance for merchant %s. Required: %s, Available: %s",
                            merchantId, amount, wallet.getBalance())
            );
        }

        // Move funds from balance to escrow
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setEscrow(wallet.getEscrow().add(amount));
        walletService.save(wallet);

        // Decrease the global balance table
        Balance balance = balanceService.findByToken(token)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Balance not found for token %s", token)
                ));

        balanceService.decreaseBalance(balance, amount);

        log.info("Successfully initiated withdrawal for merchant {} withdrawal {}", merchantId, withdrawalId);
    }

    @Transactional
    public void completeWithdrawal(UUID merchantId, UUID withdrawalId, BigInteger amount, Token token) {
        log.info("Completing withdrawal for merchant {} withdrawal {} amount {} token {}",
                merchantId, withdrawalId, amount, token);

        // Find wallet for merchant and token
        Wallet wallet = walletService.getWalletsByMerchantId(merchantId).stream()
                .filter(w -> w.getToken().equals(token.getValue()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Wallet not found for merchant %s and token %s", merchantId, token)
                ));

        // Remove funds from escrow (withdrawal completed)
        wallet.setEscrow(wallet.getEscrow().subtract(amount));
        walletService.save(wallet);

        log.info("Successfully completed withdrawal for merchant {} withdrawal {}", merchantId, withdrawalId);
    }

    @Transactional
    public void cancelWithdrawal(UUID merchantId, UUID withdrawalId, BigInteger amount, Token token) {
        log.info("Cancelling withdrawal for merchant {} withdrawal {} amount {} token {}",
                merchantId, withdrawalId, amount, token);

        // Find wallet for merchant and token
        Wallet wallet = walletService.getWalletsByMerchantId(merchantId).stream()
                .filter(w -> w.getToken().equals(token.getValue()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Wallet not found for merchant %s and token %s", merchantId, token)
                ));

        // Move funds back from escrow to balance
        wallet.setEscrow(wallet.getEscrow().subtract(amount));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletService.save(wallet);

        // Restore the global balance table
        Balance balance = balanceService.findByToken(token)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Balance not found for token %s", token)
                ));

        balanceService.increaseBalance(balance, amount);

        log.info("Successfully cancelled withdrawal for merchant {} withdrawal {}", merchantId, withdrawalId);
    }
}
