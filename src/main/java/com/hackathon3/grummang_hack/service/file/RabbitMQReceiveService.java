package com.hackathon3.grummang_hack.service.file;

import com.hackathon3.grummang_hack.config.RabbitMQProperties;
import com.hackathon3.grummang_hack.model.dto.Result;
import com.hackathon3.grummang_hack.service.vt.VtAnalysisService;
import com.hackathon3.grummang_hack.service.vt.VtReportSaveService;
import com.hackathon3.grummang_hack.service.vt.VtUploadService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQReceiveService {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQReceiveService.class);

    private final RabbitMQProperties properties;
    private final VtUploadService vtUploadService;
    private final VtReportSaveService vtReportSaveService;
    private final VtAnalysisService vtAnalysisService;
    private final FileGroupService fileGroupService;

    public RabbitMQReceiveService(RabbitMQProperties properties,
                                  VtUploadService vtUploadService, VtReportSaveService vtReportSaveService, VtAnalysisService vtAnalysisService
    ,FileGroupService fileGroupService){
        this.properties = properties;
        this.vtUploadService = vtUploadService;
        this.vtReportSaveService = vtReportSaveService;
        this.vtAnalysisService = vtAnalysisService;
        this.fileGroupService = fileGroupService;
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.vtReportQueue}")
    public void onVtReportRequestReceived(long fileId) {
        try {
            logger.info("Received VT report request for file ID: {}", fileId);
            vtReportSaveService.processReport(fileId);
        } catch (Exception e) {
            logger.error("Failed to process VT report request for file ID: {}", fileId, e);
        }
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.vtUploadQueue}")
    public void onVtUploadRequestReceived(long fileId){
        try {
            Result result = vtUploadService.uploadFileFromS3(fileId);
            if(result.isSuccess()) {
                logger.info("analysis: {}, file: {}", result.getMessage(), fileId);
                vtAnalysisService.pollAnalysisStatus(result.getMessage(), fileId);
            }
        } catch (Exception e) {
            logger.error("Failed to process Vt upload request for file ");
        }
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.groupingQueue}")
    public void onActivitiesRequestReceived(long eventId) {
        log.info("received : {}", eventId);
        fileGroupService.groupFilesAndSave(eventId);
    }

}

