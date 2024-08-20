package com.hackathon3.grummang_hack.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class VtRequestDto {
    private List<Long> fileIds;

    public VtRequestDto(List<Long> fileIds){
        this.fileIds = fileIds;
    }
}
