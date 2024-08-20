package com.hackathon3.grummang_hack.model.dto.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FileRelationNodes {
    private long eventId;
    private String saas;
    private String eventType;
    private String fileName;
    private String hash256;
    private String saasFileId;
    private LocalDateTime eventTs;
    private String email;
    private String uploadChannel;
    private double similarity;
}
