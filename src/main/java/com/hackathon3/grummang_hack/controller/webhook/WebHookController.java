package com.hackathon3.grummang_hack.controller.webhook;


import com.hackathon3.grummang_hack.model.dto.slack.event.*;
import com.hackathon3.grummang_hack.service.webhook.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhook")
@Slf4j
@RequiredArgsConstructor
public class WebHookController {


    private final WebhookUtil webhookUtil;
    private final EventHandler eventHandler;
    private final SlackFileEvent slackFileEvent;
    private final SlackChannelEvent slackChannelEvent;
    private final SlackUserEvent slackUserEvent;

    @PostMapping("/slack/{org_webhook_url}")
    public ResponseEntity<String> handleSlackEvent(@RequestBody Map<String, Object> payload, @PathVariable String org_webhook_url) {
        log.info("Received Slack event: {}", payload);

        try {
            String type = (String) payload.get("type");
            if ("url_verification".equals(type)) {
                String challenge = (String) payload.get("challenge");
                return ResponseEntity.ok(challenge);
            }

            Map<String, Object> eventMap = webhookUtil.castToMap(payload.get("event"));
            String eventType = (String) eventMap.get("type");
            String teamId = (String) payload.get("team_id");
            log.info("payload: {}", payload);
            switch (eventType) {
                case "file_shared" -> {
                    SlackFileSharedEventDto fileSharedEventDto = webhookUtil.convertToFileSharedEventDto(eventMap, teamId, org_webhook_url);
                    log.info("File shared event: {}", fileSharedEventDto);
                    slackFileEvent.handleFileEvent(webhookUtil.castToMapJson(fileSharedEventDto), "file_upload");
                }
                case "member_joined_channel" -> {
                    SlackMemberJoinedChannelEventDto memberJoinedChannelEventDto = webhookUtil.convertToMemberJoinedChannelEventDto(eventMap, teamId,org_webhook_url);
                    SlackUserJoinedEventDto userJoinedEventDto = webhookUtil.convertToUserJoinedEventDto(eventMap, teamId, org_webhook_url);
                    slackUserEvent.handleUserEvent(userJoinedEventDto);
                }
                case "channel_created" -> {
                    SlackChannelCreatedEventDto channelCreatedEventDto = webhookUtil.convertToChannelCreatedEventDto(eventMap, teamId,org_webhook_url);
                    slackChannelEvent.handleChannelEvent(eventMap);
                }
                case "team_join" -> {
                    SlackUserJoinedEventDto userJoinedEventDto = webhookUtil.convertToUserJoinedEventDto(eventMap, teamId, org_webhook_url);
                    slackUserEvent.handleUserEvent(userJoinedEventDto);
                }
                case "file_change" ->{
                    SlackFileChangeEventDto fileChangeEventDto = webhookUtil.convertToFileChangeEventDto(eventMap, teamId, org_webhook_url);
                    log.info("File change event: {}", fileChangeEventDto);
                    slackFileEvent.handleFileEvent(webhookUtil.castToMapJson(fileChangeEventDto), "file_change");
                }
                case "file_deleted" -> {
                    SlackFileDeletedEventDto fileDeletedEventDto = webhookUtil.convertToFileDeletedEventDto(eventMap, teamId, org_webhook_url);
                    log.info("File deleted event: {}", fileDeletedEventDto);
                    slackFileEvent.handleFileDeleteEvent(webhookUtil.castToMapJson(fileDeletedEventDto));
                }
                default -> log.warn("Unsupported event type: {}", eventType);
            }

            return ResponseEntity.ok("Event received successfully");
        } catch (Exception e) {
            log.error("Error processing event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing event");
        }
    }

}
