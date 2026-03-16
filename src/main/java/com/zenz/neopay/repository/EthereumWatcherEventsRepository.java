package com.zenz.neopay.repository;

import com.zenz.neopay.entity.EthereumWatcherEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EthereumWatcherEventsRepository extends JpaRepository<EthereumWatcherEvents, UUID> {

    Optional<EthereumWatcherEvents> findByBlockHash(String blockHash);

    List<EthereumWatcherEvents> findByBlockNumber(BigInteger blockNumber);

    Optional<EthereumWatcherEvents> findByBlockHashAndBlockNumber(String blockHash, BigInteger blockNumber);

    List<EthereumWatcherEvents> findByTimestampGreaterThanEqualOrderByTimestampAsc(BigInteger timestamp);
}