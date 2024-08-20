package com.hackathon3.grummang_hack.model.dto.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileHistoryStatistics {
    private String date;
    private int uploadCount;
    private int deletedCount;
    private int modifyCount;
    private int movedCount;
}
