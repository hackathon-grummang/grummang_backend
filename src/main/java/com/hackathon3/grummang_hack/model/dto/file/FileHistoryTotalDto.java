package com.hackathon3.grummang_hack.model.dto.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class FileHistoryTotalDto {
    private int totalUpload;
    private int totalDeleted;
    private int totalChanged;
    private int totalMoved;
    private List<FileHistoryStatistics> fileHistoryStatistics;
}
