package com.hackathon3.grummang_hack.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class VtRequestDto {
    private List<Long> fileIds;
}
