package com.hackathon3.grummang_hack.model.dto.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TotalTypeDto {
    private String type;
    private Long count;

    public TotalTypeDto(String type, Long count){
        this.type = type;
        this.count = count;
    }
}
