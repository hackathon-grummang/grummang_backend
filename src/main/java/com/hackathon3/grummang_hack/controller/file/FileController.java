package com.hackathon3.grummang_hack.controller.file;

import com.hackathon3.grummang_hack.model.dto.FileListResponse;
import com.hackathon3.grummang_hack.model.dto.ResponseDto;
import com.hackathon3.grummang_hack.model.dto.file.*;
import com.hackathon3.grummang_hack.service.file.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileBoardReturnService fileBoardReturnService;
    private final FileHistoryService fileHistoryService;
    private final FileHistoryStatisticsService fileHistoryStatisticsService;
    private final FileVisualizeTestService fileVisualizeTestService;
    private final FileScanListService fileScanListService;

    @Autowired
    public FileController(FileBoardReturnService fileBoardReturnService, FileHistoryService fileHistoryService, FileHistoryStatisticsService fileHistoryStatisticsService,
                          FileVisualizeTestService fileVisualizeTestService, FileScanListService fileScanListService){
        this.fileBoardReturnService = fileBoardReturnService;
        this.fileHistoryService = fileHistoryService;
        this.fileHistoryStatisticsService = fileHistoryStatisticsService;
        this.fileVisualizeTestService = fileVisualizeTestService;
        this.fileScanListService = fileScanListService;
    }
    @GetMapping
    public String hello(){
        return "Hello, file world !!";
    }

    @PostMapping("/board")
    public ResponseDto<FileDashboardDto> fileDashboardList(@RequestBody OrgIdRequest orgIdRequest){
        try {
            long orgId = orgIdRequest.getOrgId();
            FileDashboardDto fileDashboard = fileBoardReturnService.boardListReturn(orgId);
            return ResponseDto.ofSuccess(fileDashboard);
        } catch (Exception e) {
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    @PostMapping("/history")
    public ResponseDto<List<FileHistoryListDto>> fileHistoryList(@RequestBody OrgIdRequest orgIdRequest) {
        try {
            long orgId = orgIdRequest.getOrgId();
            List<FileHistoryListDto> fileHistory = fileHistoryService.historyListReturn(orgId);
            return ResponseDto.ofSuccess(fileHistory);
        } catch (Exception e) {
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    @PostMapping("/history/statistics")
    public ResponseDto<FileHistoryTotalDto> fileHistoryStatisticsList(@RequestBody OrgIdRequest orgIdRequest){
        try {
            long orgId = orgIdRequest.getOrgId();
            FileHistoryTotalDto fileHistoryStatistics = fileHistoryStatisticsService.eventStatistics(orgId);
            return ResponseDto.ofSuccess(fileHistoryStatistics);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    @PostMapping("/history/visualize")
    public ResponseDto<FileHistoryBySaaS> fileHistoryVisualize(@RequestBody EventIdRequest eventIdRequest){
        try {
            long eventId = eventIdRequest.getEventId();
            long orgId = eventIdRequest.getOrgId();
            FileHistoryBySaaS fileHistoryBySaaS = fileVisualizeTestService.getFileHistoryBySaaS(eventId, orgId);
            return ResponseDto.ofSuccess(fileHistoryBySaaS);
        } catch (Exception e) {
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    @PostMapping("/scan")
    public ResponseDto<FileListResponse> getFileList(@RequestBody OrgIdRequest orgIdRequest) {
        try {
            long orgId = orgIdRequest.getOrgId();
            FileListResponse fileListResponse = fileScanListService.getFileList(orgId);
            return ResponseDto.ofSuccess(fileListResponse);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }
}

