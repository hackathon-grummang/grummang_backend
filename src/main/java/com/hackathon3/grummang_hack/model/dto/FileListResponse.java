package com.hackathon3.grummang_hack.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
public class FileListResponse {
    private int total;
    private int malwareTotal;
    private List<FileListDto> files;

    // Constructor that uses defensive copying for mutable fields
    public FileListResponse(int total, int malwareTotal, List<FileListDto> files) {
        this.total = total;
        this.malwareTotal = malwareTotal;
        this.files = files != null ? new ArrayList<>(files) : Collections.emptyList(); // Defensive copy
    }

    public List<FileListDto> getFiles() {
        return Collections.unmodifiableList(files); // Return an unmodifiable view
    }

    public static FileListResponse of(int total, int malwareTotal, List<FileListDto> files) {
        return new FileListResponse(total, malwareTotal, files);
    }
}
