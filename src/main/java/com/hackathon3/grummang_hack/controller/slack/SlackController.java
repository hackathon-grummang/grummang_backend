package com.hackathon3.grummang_hack.controller.slack;

import com.hackathon3.grummang_hack.model.dto.slack.TopUserDTO;
import com.hackathon3.grummang_hack.model.dto.slack.file.SlackFileCountDto;
import com.hackathon3.grummang_hack.model.dto.slack.file.SlackFileSizeDto;
import com.hackathon3.grummang_hack.model.dto.slack.file.SlackRecentFileDtO;
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
import java.util.Map;

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


    @PostMapping("/init/channel")
    public ResponseEntity<Map<String, String>> SlackinitChannle(@RequestBody RequestData request){
        Map<String,String> response = new HashMap<>();
        try{
            String email = request.getEmail();
            int workespace_id = request.getWorkespace_id();
            if (email == null || workespace_id == 0){
                response.put("status","error");
                response.put("message","Invalid request");
                return ResponseEntity.badRequest().body(response);
            }


        } catch (Exception e){
            log.error("Error fetching conversations", e);
            response.put("status","error");
            response.put("message","Error fetching conversations");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        return null;
    }

    @PostMapping("/init/users")
    public ResponseEntity<Map<String, String>> SlackinitUsers(){
        Map<String,String> response = new HashMap<>();



        return null;
    }

    @PostMapping("/init/files")
    public ResponseEntity<Map<String, String>> SlackinitFiles(){
        Map<String,String> response = new HashMap<>();
        return null;
    }

    @PostMapping("/init/all")
    public ResponseEntity<Map<String, String>> SlackinitAll(){
        Map<String,String> response = new HashMap<>();
        return null;
    }
    @PostMapping("/board/files/recent")
    public ResponseEntity<SlackRecentFileDtO> SlackBoardFilesRecent(){

        return null;
    }
    @PostMapping("/board/files/size")
    public ResponseEntity<SlackFileSizeDto> SlackBoardFilesSize(){

        return null;
    }
    @PostMapping("/board/files/count")
    public ResponseEntity<SlackFileCountDto> SlackBoardFilesCount(){

        return null;
    }

    @PostMapping("/board/user-ranking")
    public ResponseEntity<TopUserDTO> SlackBoardUserRanking(){

        return null;
    }
}
