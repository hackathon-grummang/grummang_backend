package com.hackathon3.grummang_hack.model.dto.google;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriveFileCountDto {
    private int totalFiles;
    private int sensitiveFiles;
    private int maliciousFiles;
    private int connectedAccounts;
}
