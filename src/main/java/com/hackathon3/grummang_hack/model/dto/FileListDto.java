package com.hackathon3.grummang_hack.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FileListDto {
        private long id;
        private String name;
        private int size;
        private String type;
        private String saas;
        private String user;
        private String path;
        private LocalDateTime date;
        private VtReportDto vtReport;
        private FileStatusDto fileStatus;
}
