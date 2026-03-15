package com.zenz.crypto_payment_gateway.service.blockchain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.websocket.WebSocketService;

import java.net.ConnectException;

@Configuration
public class EthereumConfig {

    @Bean(destroyMethod = "shutdown")
    public Web3j web3j(@Value("${ethereum.ws-url}") String wsUrl) throws ConnectException {
        WebSocketService service = new WebSocketService(wsUrl, true);
        service.connect();
        return Web3j.build(service);
    }
}
