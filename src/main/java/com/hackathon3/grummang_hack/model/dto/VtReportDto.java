package com.hackathon3.grummang_hack.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VtReportDto {
    private String type;
    private String v3;
    private String alyac;
    private String kaspersky;
    private String falcon;
    private String avast;
    private String sentinelone;
    private int detectEngine;
    private int completeEngine;
    private int score;
    private String threatLabel;
    private String reportUrl;
}
