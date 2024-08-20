package com.hackathon3.grummang_hack.model.dto.google;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriveFileSharedEventDto {
    private String from;
    private String event;
    private String saas;
    private String fileId;
}
