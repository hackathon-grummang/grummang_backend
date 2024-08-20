package com.hackathon3.grummang_hack.service.webhook;


import com.hackathon3.grummang_hack.model.entity.Activities;
import com.hackathon3.grummang_hack.model.entity.MonitoredUsers;
import com.hackathon3.grummang_hack.model.entity.OrgSaaS;
import com.hackathon3.grummang_hack.model.mapper.slack.SlackFileMapper;
import com.hackathon3.grummang_hack.repository.ActivitiesRepo;
import com.hackathon3.grummang_hack.repository.FileUploadTableRepo;
import com.hackathon3.grummang_hack.repository.MonitoredUsersRepo;
import com.hackathon3.grummang_hack.repository.OrgSaaSRepo;
import com.hackathon3.grummang_hack.service.slack.SlackApiService;
import com.hackathon3.grummang_hack.service.slack.SlackUtil;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackFileEvent {

    private final SlackUtil fileService;
    private final SlackApiService slackApiService;
    private final OrgSaaSRepo orgSaaSRepo;
    private final FileUploadTableRepo fileUploadRepository;
    private final SlackFileMapper slackFileMapper;
    private final ActivitiesRepo fileActivityRepo;
    private final MonitoredUsersRepo slackUserRepo;

    public void handleFileEvent(Map<String, Object> payload, String event_type) {
        log.info("Handling file event with payload: {}", payload);
        try {
            String spaceId = payload.get("teamId").toString();
            String fileId = payload.get("fileId").toString();
            String event = event_type;
            if (payload.containsKey("timestamp")) {
                StringBuilder eventBuilder = new StringBuilder(event);
                event = eventBuilder.append(":").append(payload.get("timestamp").toString()).toString();
            }
            log.info("event : {}", event);

            OrgSaaS orgSaaSObject = orgSaaSRepo.findBySpaceIdUsingQuery(spaceId).orElse(null);
            File fileInfo = slackApiService.fetchFileInfo(fileId, orgSaaSObject.getId());
            if (fileInfo.getMode() == "quip" || fileInfo.getPrettyType() == "캔버스" || fileInfo.getPrettyType() == "canvas"){
                log.info("File is a quip or canvas file, skipping processing");
                return;
            }
            fileService.processAndStoreFile(fileInfo, orgSaaSObject, orgSaaSObject.getId(), event);

            log.info("File event processed successfully for file ID: {}", fileInfo.getId());
        } catch (SlackApiException | IOException e) {
            log.error("Error fetching file info or processing file data", e);
        } catch (Exception e) {
            log.error("Unexpected error processing file event", e);
        }
    }

    public void handleFileDeleteEvent(Map<String, Object> payload) {
       try {
           log.info("event_type : {}", payload.get("event"));
           // 1. activities 테이블에 deleted 이벤트로 추가
           String file_id = payload.get("fileId").toString();
           String event_ts = payload.get("timestamp").toString();
           String file_owner_id= null, file_name = null;

           long timestamp = Long.parseLong(event_ts.split("\\.")[0]);

           int org_saas_id = fileUploadRepository.findOrgSaaSIdByFileId(Long.parseLong(file_id));


           try {
               file_owner_id = fileActivityRepo.findUserBySaasFileId(file_id).orElse(null);
           } catch (Exception e) {
               log.error("Error fetching file owner ID", e);
           }
           try {
               file_name = fileActivityRepo.findFileNamesBySaasFileId(file_id).orElse(null);
           } catch (Exception e) {
                log.error("Error fetching file name", e);
           }

           MonitoredUsers userObject = slackUserRepo.findByUserIdAndOrgSaaSId(file_owner_id, org_saas_id).orElse(null);
           Activities activities = slackFileMapper.toActivityEntitiyForDeleteEvent(file_id, "file_delete", userObject, file_name, timestamp);
           fileActivityRepo.save(activities);
           // 2. file_upload 테이블에서 deleted 컬럼을 true로 변경
           fileUploadRepository.checkDelete(payload.get("fileId").toString());
       } catch (Exception e) {
           log.error("Error processing file delete event", e);
       }
    }
}
