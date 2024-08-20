package com.hackathon3.grummang_hack.model.dto.slack.event;

import lombok.Data;

@Data
public class SlackUserJoinedEventDto {
    private String from;
    private String event;
    private String saas;
    private String teamId;
    private String joinedUserId;
}
