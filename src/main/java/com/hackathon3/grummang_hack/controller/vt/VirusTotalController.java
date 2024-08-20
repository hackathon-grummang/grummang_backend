package com.hackathon3.grummang_hack.controller.vt;

import com.hackathon3.grummang_hack.config.RabbitMQProperties;
import com.hackathon3.grummang_hack.model.dto.ResponseDto;
import com.hackathon3.grummang_hack.model.dto.VtRequestDto;
import com.hackathon3.grummang_hack.model.dto.VtUploadResponse;
import com.hackathon3.grummang_hack.repository.FileUploadTableRepo;
import com.hackathon3.grummang_hack.service.vt.FileStatusService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

    @RestController
    @RequestMapping("/api/v1/vt")
    public class VirusTotalController {

        private final FileStatusService fileStatusService;
        private final RabbitTemplate rabbitTemplate;
        private final RabbitMQProperties properties;
        private final FileUploadTableRepo fileUploadRepo;

        @Autowired
        public VirusTotalController(FileStatusService fileStatusService, RabbitTemplate rabbitTemplate, RabbitMQProperties properties, FileUploadTableRepo fileUploadRepo) {
            this.fileStatusService = fileStatusService;
            this.rabbitTemplate = rabbitTemplate;
            this.properties = properties;
            this.fileUploadRepo = fileUploadRepo;
        }

        @GetMapping
        public String hello(){
            return "Hello vt world";
        }

        @PostMapping("/upload")
        public ResponseDto<List<VtUploadResponse>> vtUpload(@RequestBody VtRequestDto vtRequestDto) {
            Map<String, Object> response = new HashMap<>();

            try {
                List<VtUploadResponse> results = new ArrayList<>();
                long orgId = vtRequestDto.getOrgId();
                // 파일 ID 목록에 대한 처리
                for (Long fileId : vtRequestDto.getFileIds()) {
                    if (!fileUploadRepo.findOrgIdByFileId(fileId).orElseThrow(() ->
                                    new NoSuchElementException("File not found with id: " + fileId))
                            .equals(orgId)) {
                        response.put("error_message", "Unauthorized access to file.");
                        return ResponseDto.ofFail(response);
                    }

                    // 파일 상태에 따른 처리
                    int vtStatus = fileStatusService.getVtStatusByFileId(fileId);
                    VtUploadResponse.VtUploadResponseBuilder responseBuilder = VtUploadResponse.builder().fileId(fileId);

                    switch (vtStatus) {
                        case 1 -> results.add(responseBuilder.analysisId("Report Already exists.").build());
                        case 0 -> results.add(responseBuilder.analysisId("Analysis is processing.").build());
                        default -> {
                            // 새 분석 요청
                            rabbitTemplate.convertAndSend(properties.getVtUploadRoutingKey(), fileId);
                            results.add(responseBuilder.analysisId("Upload Request Success.").build());
                        }
                    }
                }
                return ResponseDto.ofSuccess(results);

            } catch (Exception e) {
                return ResponseDto.ofFail(e.getMessage());
            }
        }

    }
