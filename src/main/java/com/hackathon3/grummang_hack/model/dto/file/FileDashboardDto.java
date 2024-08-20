package com.hackathon3.grummang_hack.model.dto.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class FileDashboardDto {
    private long totalCount;
    private long totalVolume;
    private int totalDlp;
    private int totalMalware;
    private List<TotalTypeDto> totalType;
    private List<StatisticsDto> statistics;

    @Builder
    public FileDashboardDto(long totalCount, long totalVolume, int totalDlp, int totalMalware, List<TotalTypeDto> totalType, List<StatisticsDto> statistics){
        this.totalCount = totalCount;
        this.totalVolume = totalVolume;
        this.totalDlp = totalDlp;
        this.totalMalware = totalMalware;
        this.totalType = List.copyOf(totalType);
        this.statistics = List.copyOf(statistics);
    }
}
