package com.hackathon3.grummang_hack.model.dto.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class FileHistoryBySaaS {
    private long originNode;
    private List<FileRelationNodes> slack;
    private List<FileRelationNodes> googleDrive;
    private List<FileRelationEdges> edges;

    public FileHistoryBySaaS(long originNode, List<FileRelationNodes> slack, List<FileRelationNodes> googleDrive, List<FileRelationEdges> edges){
        this.originNode = originNode;
        this.slack = List.copyOf(slack);
        this.googleDrive = List.copyOf(googleDrive);
        this.edges = List.copyOf(edges);
    }
}
