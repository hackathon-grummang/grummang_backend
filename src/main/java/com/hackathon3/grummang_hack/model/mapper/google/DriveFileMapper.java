package com.hackathon3.grummang_hack.model.mapper.google;


import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.hackathon3.grummang_hack.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Component
@Slf4j
@RequiredArgsConstructor
public class DriveFileMapper {


    @Value("${aws.s3.bucket}")
    private String bucketName;

    public StoredFile toStoredFileEntity(File file, String hash, String filePath) {
        if (file == null) {
            return null;
        }
        return StoredFile.builder()
                .type(file.getFileExtension())
                .size(file.getSize().intValue())
                .savePath(bucketName + "/" + filePath)
                .saltedHash(hash)
                .build();
    }

    public FileUploadTable toFileUploadEntity(File file, OrgSaaS orgSaas, String hash) {
        if (file == null) {
            return null;
        }
        return FileUploadTable.builder()
                .orgSaaS(orgSaas)
                .saasFileId(file.getId())
                .hash(hash)
                .deleted(false)
                .timestamp(LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(file.getCreatedTime().getValue()),
                                ZoneId.systemDefault()))
                .build();
    }


    public Activities toActivityEntity(File file, String eventType, MonitoredUsers user, String channel) {
        if (file == null) {
            return null;
        }

        // 생성 시간의 null 체크
        DateTime createdTime = file.getCreatedTime();
        LocalDateTime eventTs = null;
        if (createdTime != null) {
            eventTs = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(createdTime.getValue()), ZoneId.systemDefault()
            );
        }

        // eventType null 체크
        if (eventType == null || eventType.isEmpty()) {
            eventType = "file_upload";
        }

        // file.getParents() null 체크
        String uploadChannel = null;
        if (file.getParents() != null && !file.getParents().isEmpty()) {
            uploadChannel = channel;
        }

        return Activities.builder()
                .user(user)
                .eventType(eventType)
                .saasFileId(file.getId())
                .fileName(file.getName())
                .eventTs(eventTs)  // eventTs가 null일 수 있음에 유의
                .uploadChannel(uploadChannel)
                .build();
    }

}
