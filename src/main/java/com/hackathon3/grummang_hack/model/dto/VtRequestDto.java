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
    private long orgId;

    public VtRequestDto(List<Long> fileIds, long orgId){
        this.fileIds = fileIds;
        this.orgId = orgId;
    }
}
