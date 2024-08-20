package com.hackathon3.grummang_hack.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileListDto {
        private long id;
        private String saltedHash;
        private int size;
        private String type;
        private VtReportDto vtReport;
        private FileStatusDto fileStatus;
}
