package com.hackathon3.grummang_hack.model.dto.google;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlackChannelEventDto {
    private String from;
    private String event;
    private String saas;
    private String channelId;
    private String channelName;
}
