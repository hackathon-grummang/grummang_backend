package com.hackathon3.grummang_hack.model.mapper.slack;

import com.hackathon3.grummang_hack.model.entity.*;
import com.hackathon3.grummang_hack.repository.MonitoredUsersRepo;
import com.slack.api.model.File;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class SlackFileMapper {


    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final MonitoredUsersRepo monitoredUsersRepo;


    public StoredFile toStoredFileEntity(File file, String hash, String filePath) {
        if (file == null) {
            return null;
        }
        return StoredFile.builder()
                .type(file.getFiletype())
                .size(file.getSize())
                .savePath(bucketName + "/" + filePath)
                .saltedHash(hash)
                .build();
    }
    public List<StoredFile> toStoredFileEntity(List<File> files, List<String> hashes, List<String> filePaths) {
        return IntStream.range(0, files.size())
                .mapToObj(i -> toStoredFileEntity(files.get(i), hashes.get(i), filePaths.get(i)))
                .collect(Collectors.toList());
    }

    public FileUploadTable toFileUploadEntity(File file, OrgSaaS orgSaas, String hash, LocalDateTime timestamp) {
        if (file == null) {
            return null;
        }
        return FileUploadTable.builder()
                .orgSaaS(orgSaas)
                .saasFileId(file.getId())
                .hash(hash)
                .timestamp(timestamp != null ? timestamp : LocalDateTime.ofInstant(Instant.ofEpochSecond(file.getTimestamp()), ZoneId.systemDefault()))
                .build();
    }


    public Activities toActivityEntity(File file, String eventType, MonitoredUsers user, String channel) {
        if (file == null) {
            return null;
        }
        return Activities.builder()
                .user(user)
                .eventType(eventType)
                .saasFileId(file.getId())
                .fileName(file.getTitle())
                .eventTs(LocalDateTime.ofInstant(Instant.ofEpochSecond(file.getTimestamp()), ZoneId.systemDefault()))
                .uploadChannel(file.getChannels().isEmpty() ? null : channel)
                .build();
    }

    public Activities toActivityEntitiyForDeleteEvent(String file_id, String eventType, MonitoredUsers user, String file_name, long timestamp){
        return Activities.builder()
                .user(user)
                .eventType(eventType)
                .saasFileId(file_id)
                .fileName(file_name)
                .eventTs(LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault()))
                .uploadChannel("deleted")
                .build();
    }
}
