package com.hackathon3.grummang_hack.model.dto.google;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DriveRecentFileDTO {
    private String fileName;
    private String uploadedBy;
    private String fileType;
    private LocalDateTime uploadTimestamp;

    public DriveRecentFileDTO(String fileName, String uploadedBy, String fileType, LocalDateTime uploadTimestamp) {
        this.fileName = fileName;
        this.uploadedBy = uploadedBy;
        this.fileType = fileType;
        this.uploadTimestamp = uploadTimestamp;
    }
}
