package com.hackathon3.grummang_hack.controller.slack;

import com.hackathon3.grummang_hack.model.dto.slack.TopUserDTO;
import com.hackathon3.grummang_hack.model.dto.slack.file.SlackFileCountDto;
import com.hackathon3.grummang_hack.model.dto.slack.file.SlackFileSizeDto;
import com.hackathon3.grummang_hack.model.dto.slack.file.SlackRecentFileDto;
import com.hackathon3.grummang_hack.model.entity.Saas;
import com.hackathon3.grummang_hack.repository.AdminUsersRepo;
import com.hackathon3.grummang_hack.repository.SaasRepo;
import com.hackathon3.grummang_hack.service.slack.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequestMapping("/api/v1/slack")
@RequiredArgsConstructor
public class SlackController {

    private final RestTemplate restTemplate;
    private final SlackApiService slackApiService;
    private final SlackUtil slackUtil;
    private final SlackChannelService slackChannelService;
    private final SlackUserService slackUserService;
    private final SlackFileService slackFileService;
    private final AdminUsersRepo adminRepo;
    private final SaasRepo saasRepo;
    private final SlackFileService fileService;


    @PostMapping("/init/channel")
    public ResponseEntity<Map<String, String>> SlackinitChannel(@RequestBody InitRequestData request){
        Map<String,String> response = new HashMap<>();
        try{
            int workespace_id = request.getWorkspace_id();
            if (workespace_id == -1){
                return INVALID_REQUEST();
            }
            slackChannelService.slackFirstChannels(workespace_id);
            response.put("status", "success");
            response.put("message", "Channel saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            log.error("Error fetching conversations", e);
            response.put("status","error");
            response.put("message","Error fetching conversations");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/init/users")
    public ResponseEntity<Map<String, String>> SlackinitUsers(@RequestBody InitRequestData request){
        Map<String,String> response = new HashMap<>();
        try {
            int workespace_id = request.getWorkspace_id();
            if ( workespace_id == -1){
                return INVALID_REQUEST();
            }
            try{
                slackUserService.slackFirstUsers(workespace_id);
                response.put("status", "success");
                response.put("message", "channel saved successfully");
                return ResponseEntity.ok(response);
            } catch (Exception e){
                log.error("Error fetching conversations", e);
                response.put("status","error");
                response.put("message","Error fetching conversations");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e){
            log.error("Error fetching conversations", e);
            response.put("status","error");
            response.put("message","Error fetching conversations");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/init/files")
    public ResponseEntity<Map<String, String>> SlackinitFiles(@RequestBody InitRequestData request){
        Map<String,String> response = new HashMap<>();
        try {
            int workespace_id = request.getWorkspace_id();
            if (workespace_id == -1){
                return INVALID_REQUEST();
            }
            slackFileService.fetchAndStoreFiles(workespace_id, "file_upload");
            response.put("status", "success");
            response.put("message", "File saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            log.error("Error fetching conversations", e);
            response.put("status","error");
            response.put("message","Error fetching conversations");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/init/all")
    public ResponseEntity<Map<String, String>> SlackinitAll(@RequestBody RequestData request) {
        Map<String, String> response = new HashMap<>();
        try {
            String email = request.getEmail();
            int workespace_id = request.getWorkespace_id();
            if (email == null || workespace_id == -1) {
                return INVALID_REQUEST();
            }

            // 순차적으로 비동기 메서드를 실행
            slackChannelService.slackFirstChannels(workespace_id).get(); // 첫 번째 메서드 실행 및 대기
            slackUserService.slackFirstUsers(workespace_id).get(); // 두 번째 메서드 실행 및 대기

            // 세 번째 메서드 실행
            slackFileService.fetchAndStoreFiles(workespace_id, "file_upload");

            response.put("status", "success");
            response.put("message", "All saved successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching conversations", e);
            response.put("status", "error");
            response.put("message", "Error fetching conversations");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/board/files/recent")
    public ResponseEntity<?> SlackBoardFilesRecent(@RequestBody RequestData request){
        try {
            String email = request.getEmail();
            int workespace_id = request.getWorkespace_id();
            if (email == null || workespace_id == -1){
                return INVALID_REQUEST();
            }
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            Saas saasObject = saasRepo.findBySaasName("Slack").orElse(null);
            List<SlackRecentFileDto> recentFiles = fileService.slackRecentFiles(orgId, saasObject.getId().intValue());
            return ResponseEntity.ok(recentFiles);
        } catch (Exception e){
            log.error("Error fetching recent file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PostMapping("/board/files/size")
    public ResponseEntity<?> SlackBoardFilesSize(@RequestBody RequestData request){
        try {
            String email = request.getEmail();
            int workespace_id = request.getWorkespace_id();
            if (email == null || workespace_id == -1){
                return INVALID_REQUEST();
            }
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            SlackFileSizeDto slackFileSizeDto = slackFileService.sumOfSlackFileSize(orgId,1);
            return ResponseEntity.ok(slackFileSizeDto);
        } catch (Exception e){
            log.error("Error fetching file size", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PostMapping("/board/files/count")
    public ResponseEntity<?> SlackBoardFilesCount(@RequestBody RequestData request){
        try {
            String email = request.getEmail();
            int workespace_id = request.getWorkespace_id();
            if (email == null || workespace_id == -1){
                return INVALID_REQUEST();
            }
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            SlackFileCountDto slackFileCountDto = slackFileService.SlackFileCountSum(orgId,1);
            return ResponseEntity.ok(slackFileCountDto);
        } catch (Exception e){
            log.error("Error fetching file count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/board/user-ranking")
    public ResponseEntity<?> SlackBoardUserRanking(@RequestBody RequestData request){
        try {
            String email = request.getEmail();
            int workespace_id = request.getWorkespace_id();
            if (email == null || workespace_id == -1){
                return INVALID_REQUEST();
            }
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            Saas saasObject = saasRepo.findBySaasName("Slack").orElse(null);
            CompletableFuture<List<TopUserDTO>> future = slackUserService.getTopUsersAsync(orgId, saasObject.getId().intValue());
            List<TopUserDTO> topuser = future.get();

            return ResponseEntity.ok(topuser);
        } catch (Exception e){
            log.error("Error fetching user ranking file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private ResponseEntity<Map<String,String>> INVALID_REQUEST(){
        Map<String,String> response = new HashMap<>();
        response.put("status","error");
        response.put("message","Invalid request");
        return ResponseEntity.badRequest().body(response);
    }
}
