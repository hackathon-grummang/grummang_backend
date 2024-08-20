package com.hackathon3.grummang_hack.service.vt;

import com.hackathon3.grummang_hack.model.dto.Result;
import com.hackathon3.grummang_hack.model.dto.VtReportDto;
import com.hackathon3.grummang_hack.model.entity.StoredFile;
import com.hackathon3.grummang_hack.model.entity.VtReport;
import com.hackathon3.grummang_hack.repository.StoredFileRepo;
import com.hackathon3.grummang_hack.repository.VtReportRepo;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

@Service
public class VtReportSaveService {

    private static final Logger logger = LoggerFactory.getLogger(VtReportSaveService.class);

    @Value("${virustotal.api.key}")
    private String apikey;

    private static final String BASE_URL = "https://www.virustotal.com/api/v3";


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;
    private final VtReportRepo reportRepository;
    private final StoredFileRepo storedFileRepository;
    private final FileStatusService fileStatusService;

    @Autowired
    public VtReportSaveService(RestTemplate restTemplate, VtReportRepo reportRepository, StoredFileRepo storedFileRepository
            , ModelMapper modelMapper, FileStatusService fileStatusService) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.reportRepository = reportRepository;
        this.storedFileRepository = storedFileRepository;
        this.modelMapper = modelMapper;
        this.fileStatusService = fileStatusService;
    }

    @Async("taskExecutor")
    public void processReport(Long fileId) {
        // VT 리포트를 가져옵니다.
        Result reportResult = saveReportById(fileId);

        // 리포트가 성공적으로 저장된 경우 상태 업데이트
        if (reportResult.isSuccess()) {
            fileStatusService.updateVtStatus(fileId, 1); // vtStatus를 1로 업데이트
        }
    }

    @Transactional
    public Result saveReportById(long fileId) {
        logger.info("Retrieving file with ID: {}", fileId);
        Optional<StoredFile> optionalStoredFile = storedFileRepository.findById(fileId);
        if (optionalStoredFile.isEmpty()) {
            return Result.builder().success(false).message("File not found").build();
        }

        StoredFile storedFile = optionalStoredFile.get();
        String hash = storedFile.getSaltedHash(); // Assuming SaltedHash is the hash value you want to retrieve
        String url = BASE_URL + "/files/" + hash;
        logger.info("{}", hash);

        // Log the URL and headers for debugging
        logger.info("API URL: {}", url);
        logger.info("API Key: {}", apikey);

        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("x-apikey", apikey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            HttpStatusCode statusCode = responseEntity.getStatusCode();

            if (statusCode == HttpStatus.NOT_FOUND) {
                return Result.builder().success(false).message("Report not found in VirusTotal").build();
            } else if (statusCode.is2xxSuccessful()) {
                String response = responseEntity.getBody();
                boolean isReportSaved = saveReportToDatabase(response, storedFile);
                if (isReportSaved) {
                    return Result.builder().success(true).message("Report saved successfully").build();
                } else {
                    return Result.builder().success(true).message("Report already exists. Skipping save operation.").build();
                }
            } else {
                return Result.builder().success(false).message("Unexpected response status: " + statusCode).build();
            }
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error: {}, Status Code: {}", e.getMessage(), e.getStatusCode());
            return Result.builder().success(false).message("HTTP error: " + e.getStatusCode()).build();
        } catch (Exception e) {
            logger.error("Unexpected error while saving report by ID: {}", fileId, e);
            return Result.builder().success(false).message("Internal server error").build();
        }
    }

    @Transactional
    public boolean saveReportToDatabase(String response, StoredFile storedFile) {
        VtReportDto reportDto = VtReportDto.builder().build();  // 기본값으로 초기화

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode attributesNode = jsonNode.path("data").path("attributes");

            reportDto = VtReportDto.builder()
                    .type(attributesNode.path("type_extension").asText())
                    .v3(extractEngineResult(attributesNode, "AhnLab-V3"))
                    .alyac(extractEngineResult(attributesNode, "ALYac"))
                    .kaspersky(extractEngineResult(attributesNode, "Kaspersky"))
                    .falcon(extractEngineResult(attributesNode, "CrowdStrike"))
                    .avast(extractEngineResult(attributesNode, "Avast"))
                    .sentinelone(extractEngineResult(attributesNode, "SentinelOne"))
                    .detectEngine(calculateDetectedEngine(attributesNode))
                    .completeEngine(calculateCompletedEngine(attributesNode))
                    .score(calculateScore(reportDto.getDetectEngine(), reportDto.getCompleteEngine()))
                    .threatLabel(extractThreatLabel(attributesNode.path("popular_threat_classification")))
                    .reportUrl("https://www.virustotal.com/gui/file/" + storedFile.getSaltedHash())
                    .build();

            VtReport report = modelMapper.map(reportDto, VtReport.class);
            report.setStoredFile(storedFile); // manually set the StoredFile reference

            // Check if a report with the same fileId already exists
            if (reportRepository.existsByStoredFileId(storedFile.getId())) {
                logger.info("Report for file ID: {} already exists. Skipping save operation.", storedFile.getId());
                return false; // 이미 존재하는 경우 false 반환
            } else {
                // Save new report
                reportRepository.save(report);
                logger.info("Saved new report for file ID: {}", storedFile.getId());
                return true; // 새로 저장한 경우 true 반환
            }

        } catch (IOException e) {
            logger.error("Failed to save report to database for file ID: {}", storedFile.getId(), e);
            return false;
        }
    }

    private int calculateDetectedEngine(JsonNode attributesNode) {
        int malicious = attributesNode.path("last_analysis_stats").path("malicious").asInt();
        int suspicious = attributesNode.path("last_analysis_stats").path("suspicious").asInt();
        return malicious + suspicious;
    }

    private int calculateCompletedEngine(JsonNode attributesNode) {
        JsonNode statsNode = attributesNode.path("last_analysis_stats");
        int malicious = statsNode.path("malicious").asInt();
        int suspicious = statsNode.path("suspicious").asInt();
        int undetected = statsNode.path("undetected").asInt();
        int harmless = statsNode.path("harmless").asInt();
        return malicious + suspicious + undetected + harmless;
    }

    private int calculateScore(int detected_engine, int completed_engine) {
        return completed_engine > 0 ? (detected_engine * 100 / completed_engine) : 0;
    }

    private String extractThreatLabel(JsonNode labelNode) {
        JsonNode threatLabelNode = labelNode.path("suggested_threat_label");
        return threatLabelNode.isMissingNode() ? "none" : threatLabelNode.asText();
    }

    private String extractEngineResult(JsonNode attributesNode, String engineName) {
        JsonNode scansNode = attributesNode.path("last_analysis_results");
        JsonNode engineNode = scansNode.path(engineName);
        if (!engineNode.isMissingNode()) {
            String category = engineNode.path("category").asText();
            if ("malicious".equals(category)) {
                return engineNode.path("result").asText();
            } else if ("undetected".equals(category)) {
                return "undetected";
            } else {
                return "unsupported";
            }

        }
        return "unsupported";
    }

}
