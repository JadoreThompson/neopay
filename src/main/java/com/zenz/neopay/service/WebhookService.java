package com.zenz.neopay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Slf4j
public class WebhookService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    
    private final BlockingQueue<WebhookTask> webhookQueue = new LinkedBlockingQueue<>();
    private ExecutorService executorService;
    private volatile boolean running = true;

    public WebhookService() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::consumeWebhooks);
        log.info("WebhookService started with background consumer thread");
    }

    public void sendRequest(Object payload, String url) {
        WebhookTask task = new WebhookTask(payload, url);
        try {
            webhookQueue.put(task);
            log.info("Webhook queued for URL: {}", url);
        } catch (InterruptedException e) {
            log.error("Failed to queue webhook for URL: {}", url, e);
            Thread.currentThread().interrupt();
        }
    }

    private void consumeWebhooks() {
        while (running || !webhookQueue.isEmpty()) {
            try {
                WebhookTask task = webhookQueue.take();
                processWebhook(task);
            } catch (InterruptedException e) {
                log.info("Webhook consumer thread interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("Webhook consumer thread stopped");
    }

    private void processWebhook(WebhookTask task) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(task.payload());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);
            
            log.info("Sending webhook POST to {} with payload: {}", task.url(), jsonPayload);
            
            ResponseEntity<String> response = restTemplate.postForEntity(task.url(), requestEntity, String.class);
            
            log.info("Webhook sent to {} - Status: {}, Response: {}", 
                    task.url(), response.getStatusCode(), response.getBody());
        } catch (Exception e) {
            log.error("Failed to send webhook to URL: {}", task.url(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down WebhookService...");
        running = false;
        executorService.shutdown();
        log.info("WebhookService shutdown complete");
    }

    private record WebhookTask(Object payload, String url) {}
}