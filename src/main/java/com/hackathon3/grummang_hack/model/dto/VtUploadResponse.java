package com.hackathon3.grummang_hack.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VtUploadResponse {
    private long fileId;
    private String analysisId;
}
