package com.hackathon3.grummang_hack.service.file;

import com.hackathon3.grummang_hack.model.dto.file.FileHistoryDto;
import com.hackathon3.grummang_hack.model.dto.file.FileHistoryListDto;
import com.hackathon3.grummang_hack.model.entity.Activities;
import com.hackathon3.grummang_hack.repository.ActivitiesRepo;
import com.hackathon3.grummang_hack.repository.FileUploadTableRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class FileHistoryService {

    private final ActivitiesRepo activitiesRepo;
    private final FileUploadTableRepo fileUploadRepo;

    public FileHistoryService(ActivitiesRepo activitiesRepo, FileUploadTableRepo fileUploadRepo){
        this.activitiesRepo = activitiesRepo;
        this.fileUploadRepo = fileUploadRepo;
    }

    // 역사 리스트 반환 메서드
    public List<FileHistoryListDto> historyListReturn(long orgId) {

        List<Activities> activitiesList = activitiesRepo.findByUser_OrgSaaS_Org_Id(orgId);

        List<FileHistoryDto> sortedHistoryList = activitiesList.stream()
                .map(this::convertToFileHistoryDto)
                .sorted(Comparator.comparing(FileHistoryDto::getEventTs))
                .toList();

        int totalEvent = sortedHistoryList.size();

        FileHistoryListDto fileHistoryListDto = FileHistoryListDto.builder()
                .totalEvent(totalEvent)
                .fileHistoryDto(sortedHistoryList)
                .build();

        return List.of(fileHistoryListDto);
    }


    private FileHistoryDto convertToFileHistoryDto(Activities activity) {
        LocalDateTime uploadTs = fileUploadRepo.findEarliestUploadTsByOrgSaaS_IdAndSaasFileId(activity.getUser().getOrgSaaS().getId(),activity.getSaasFileId());
        return FileHistoryDto.builder()
                .eventId(activity.getId())
                .saas(activity.getUser().getOrgSaaS().getSaas().getSaasName())
                .eventType(activity.getEventType())
                .fileName(activity.getFileName())
                .saasFileId(activity.getSaasFileId())
                .uploadTs(uploadTs)
                .eventTs(activity.getEventTs())
                .email(activity.getUser().getEmail())
                .uploadChannel(activity.getUploadChannel())
                .build();
    }
}
