package com.hackathon3.grummang_hack.model.dto;

import com.hackathon3.grummang_hack.model.dto.file.FileHistoryBySaaS;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class FileStatusDto {
    private int vtStatus;

    public FileStatusDto(int vtStatus){
        this.vtStatus = vtStatus;
    }
}
