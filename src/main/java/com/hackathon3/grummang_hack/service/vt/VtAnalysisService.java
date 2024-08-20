package com.hackathon3.grummang_hack.service.vt;

import com.hackathon3.grummang_hack.config.RabbitMQProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.Async;


@Service
public class VtAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(VtAnalysisService.class);
    private final RestTemplate restTemplate;
    private final FileStatusService fileStatusService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitMQProperties properties;

    @Value("${virustotal.api.key}")
    private String apikey;

    private static final String BASE_URL = "https://www.virustotal.com/api/v3/analyses/";
    private static final int RETRY_INTERVAL = 10000;

    @Autowired
    public VtAnalysisService(RestTemplate restTemplate, FileStatusService fileStatusService, ObjectMapper objectMapper, RabbitTemplate rabbitTemplate, RabbitMQProperties properties) {
        this.restTemplate = restTemplate;
        this.fileStatusService = fileStatusService;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Async("taskExecutor")
    public void pollAnalysisStatus(String analysisId, long fileId) {
        int retries = 0;
        boolean isCompleted = false;

        while (!isCompleted) {
            try {
                Thread.sleep(RETRY_INTERVAL);
                isCompleted = checkAnalysisStatus(analysisId, fileId);

                if (isCompleted) {
                    logger.info("Analysis completed for fileId: {}", fileId);
                } else {
                    logger.info("Analysis not yet complete for fileId: {}, retrying... (Attempt {})", fileId, retries + 1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted during sleep for fileId: {}", fileId, e);
                return;
            } catch (Exception e) {
                logger.error("Unexpected error occurred while checking analysis status for fileId: {}", fileId, e);
            }

            retries++;
        }
    }

    @Transactional
    public boolean checkAnalysisStatus(String analysisId, long fileId) {
        String url = BASE_URL + analysisId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-apikey", apikey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String status = jsonNode.path("data").path("attributes").path("status").asText();

                if ("completed".equals(status)) {
                    fileStatusService.updateVtStatus(fileId, 1);
                    rabbitTemplate.convertAndSend(properties.getVtReportRoutingKey(), fileId);
                    return true;
                }

            } catch (Exception e) {
                logger.error("Error parsing response", e);
            }
        } else {
            logger.error("Error fetching analysis status: " + response.getStatusCode());
        }

        return false;
    }
}

