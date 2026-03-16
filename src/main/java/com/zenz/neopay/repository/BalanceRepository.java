package com.zenz.neopay.repository;

import com.zenz.neopay.entity.Balance;
import com.zenz.neopay.enums.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, UUID> {

    Balance findByToken(Token token);
}