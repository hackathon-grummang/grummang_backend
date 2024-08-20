package com.hackathon3.grummang_hack.model.dto.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class FileHistoryListDto {
    private int totalEvent;
    private List<FileHistoryDto> fileHistoryDto;
}
