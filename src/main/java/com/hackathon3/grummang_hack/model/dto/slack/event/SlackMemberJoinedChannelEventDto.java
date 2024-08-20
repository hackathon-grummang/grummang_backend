package com.hackathon3.grummang_hack.model.dto.slack.event;

import lombok.Data;

@Data
public class SlackMemberJoinedChannelEventDto {
    private String from;
    private String event;
    private String saas;
    private String token;
    private String teamId;
    private String apiAppId;
    private String joinedUser;
    private String joinedChannel;
    private String timestamp;
}
