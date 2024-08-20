package com.hackathon3.grummang_hack.model.dto.slack.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlackFileSharedEventDto {
    private String from;
    private String event;
    private String saas;
    private String fileId;
}
