package com.hackathon3.grummang_hack.service.google;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.hackathon3.grummang_hack.model.entity.OrgSaaS;
import com.hackathon3.grummang_hack.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class DriveFileService {

    public final DriveApiService driveApiService;
    public final GoogleDriveUtil googleDriveUtil;
    public final OrgSaaSRepo orgSaaSRepo;
    public final GoogleUtilService googleUtilService;
    public final WorkSpaceConfigRepo workspaceConfigRepo;
    public final FileUploadTableRepo fileUploadRepository;
    public final StoredFileRepo storedFilesRepository;
    public final MonitoredUsersRepo monitoredUserRepo;
    @Autowired
    public DriveFileService(DriveApiService driveApiService, GoogleDriveUtil googleDriveUtil, OrgSaaSRepo orgSaaSRepo
            , GoogleUtilService googleUtilService, WorkSpaceConfigRepo worekSpaceRepo
            , StoredFileRepo storedFilesRepository, FileUploadTableRepo fileUploadRepository
            , MonitoredUsersRepo monitoredUserRepo) {
        this.driveApiService = driveApiService;
        this.googleDriveUtil = googleDriveUtil;
        this.orgSaaSRepo = orgSaaSRepo;
        this.googleUtilService = googleUtilService;
        this.workspaceConfigRepo = worekSpaceRepo;
        this.storedFilesRepository = storedFilesRepository;
        this.fileUploadRepository = fileUploadRepository;
        this.monitoredUserRepo = monitoredUserRepo;
    }

    @Transactional
    public CompletableFuture<Void> fetchAndStoreFiles(int workspaceId, String eventType) {
        return CompletableFuture.runAsync(() -> {
            try {
                Drive service = googleUtilService.getDriveService(workspaceId);
                OrgSaaS orgSaaSObject = orgSaaSRepo.findById(workspaceId).orElse(null);

                String spaceId = orgSaaSRepo.getSpaceID(workspaceId);
                List<File> fileList = driveApiService.fetchFiles(service, spaceId).getFiles();

                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (File file : fileList) {
                    if (shouldSkipFile(file)) {
                        log.info("Skipping unsupported file: {}, {}", file.getId(), file.getName());
                        continue;
                    }
                    CompletableFuture<Void> future = googleDriveUtil.processAndStoreFile(file, orgSaaSObject, workspaceId, eventType, service);
                    futures.add(future);
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            } catch (Exception e) {
                log.error("Error processing files", e);
            }
        });
    }


    private boolean shouldSkipFile(File file) {
        return "application/vnd.google-apps.spreadsheet".equalsIgnoreCase(file.getMimeType()) ||  // Google Sheets
                "application/vnd.google-apps.document".equalsIgnoreCase(file.getMimeType()) ||    // Google Docs
                "application/vnd.google-apps.presentation".equalsIgnoreCase(file.getMimeType());  // Google Slides
    }



}