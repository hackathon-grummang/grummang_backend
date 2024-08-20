package com.hackathon3.grummang_hack.service.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon3.grummang_hack.model.dto.slack.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class WebhookUtil {


    public SlackMemberJoinedChannelEventDto convertToMemberJoinedChannelEventDto(Map<String, Object> eventMap, String teamId, String org_webhook_url) {
        SlackMemberJoinedChannelEventDto dto = new SlackMemberJoinedChannelEventDto();
        dto.setFrom(org_webhook_url);
        dto.setEvent((String) eventMap.get("type"));
        dto.setToken((String) eventMap.get("token"));
        dto.setTeamId(teamId);
        dto.setApiAppId((String) eventMap.get("api_app_id"));
        dto.setJoinedUser((String) eventMap.get("user"));
        dto.setJoinedChannel((String) eventMap.get("channel"));
        dto.setTimestamp((String) eventMap.get("event_ts"));
        return dto;
    }

    public SlackChannelCreatedEventDto convertToChannelCreatedEventDto(Map<String, Object> eventMap, String teamId, String org_webhook_url) {
        SlackChannelCreatedEventDto dto = new SlackChannelCreatedEventDto();
        dto.setFrom(org_webhook_url);
        dto.setEvent((String) eventMap.get("type"));
        dto.setSaas("slack");
        dto.setTeamId(teamId);
        dto.setChannelId((String) eventMap.get("channel_id"));
        return dto;
    }

    public SlackUserJoinedEventDto convertToUserJoinedEventDto(Map<String, Object> eventMap, String teamId, String org_webhook_url) {
        Map<String, Object> user = castToMap(eventMap.get("user"));
        SlackUserJoinedEventDto dto = new SlackUserJoinedEventDto();
        dto.setFrom(org_webhook_url);
        dto.setEvent((String) eventMap.get("type"));
        dto.setSaas("slack");
        dto.setTeamId(teamId);
        dto.setJoinedUserId((String) user.get("id"));
        return dto;
    }
    @SuppressWarnings("unchecked")
    public Map<String, Object> castToMap(Object object) {
        return (Map<String, Object>) object;
    }
    public Map<String, Object> castToMapJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(object, Map.class);
    }

    public SlackFileSharedEventDto convertToFileSharedEventDto(Map<String, Object> eventMap, String teamId, String org_webhook_url) {
        SlackFileSharedEventDto dto = new SlackFileSharedEventDto();
        dto.setFrom(org_webhook_url);
        dto.setEvent("file_upload");
        dto.setSaas("slack");
        dto.setTeamId(teamId);
        dto.setFileId((String) eventMap.get("file_id"));
        return dto;
    }

    public SlackFileChangeEventDto convertToFileChangeEventDto(Map<String, Object> eventMap, String teamId, String orgWebhookUrl) {
        SlackFileChangeEventDto dto = new SlackFileChangeEventDto();
        dto.setFrom(orgWebhookUrl);
        dto.setEvent("file_change");
        dto.setSaas("slack");
        dto.setTeamId(teamId);
        dto.setFileId((String) eventMap.get("file_id"));
        dto.setWho((String) eventMap.get("user_id"));
        dto.setTimestamp((String) eventMap.get("event_ts"));
        return dto;
    }

    public SlackFileDeletedEventDto convertToFileDeletedEventDto(Map<String, Object> eventMap, String teamId, String orgWebhookUrl) {
        SlackFileDeletedEventDto dto = new SlackFileDeletedEventDto();
        dto.setFrom(orgWebhookUrl);
        dto.setEvent("file_delete");
        dto.setSaas("slack");
        dto.setTeamId(teamId);
        dto.setFileId((String) eventMap.get("file_id"));
        dto.setTimestamp((String) eventMap.get("event_ts"));
        return dto;
    }

}
