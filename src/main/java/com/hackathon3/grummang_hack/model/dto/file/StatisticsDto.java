package com.hackathon3.grummang_hack.model.dto.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StatisticsDto {
    private String date;
    private long volume;
    private long count;

    @Builder
    public StatisticsDto(String date, long volume, long count){
        this.date = date;
        this.volume = volume;
        this.count = count;
    }
}
