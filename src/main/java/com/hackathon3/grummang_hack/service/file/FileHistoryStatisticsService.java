package com.hackathon3.grummang_hack.service.file;

import com.hackathon3.grummang_hack.model.dto.file.FileHistoryStatistics;
import com.hackathon3.grummang_hack.model.dto.file.FileHistoryTotalDto;
import com.hackathon3.grummang_hack.repository.ActivitiesRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileHistoryStatisticsService {
    private final ActivitiesRepo activitiesRepo;

    public FileHistoryStatisticsService(ActivitiesRepo activitiesRepo){
        this.activitiesRepo = activitiesRepo;
    }

    public FileHistoryTotalDto eventStatistics(long orgId){
        int totalUpload = getTotalUploadCount(orgId);
        int totalDeleted = getTotalDeletedCount(orgId);
        int totalChanged = getTotalChangedCount(orgId);
        int totalMoved = getTotalMovedCount(orgId);

        // FileHistoryStatistics 리스트 생성
        List<FileHistoryStatistics> fileHistoryStatisticsList = getFileHistoryStatisticsDay(orgId);

        // FileHistoryTotalDto 객체 생성 및 반환
        return FileHistoryTotalDto.builder()
                .totalUpload(totalUpload)
                .totalDeleted(totalDeleted)
                .totalChanged(totalChanged)
                .totalMoved(totalMoved)
                .fileHistoryStatistics(fileHistoryStatisticsList)  // 리스트로 변경된 부분
                .build();
    }

    private int getTotalUploadCount(long orgId){
        return activitiesRepo.findTotalUploadCount(orgId);
    }

    private int getTotalDeletedCount(long orgId){
        return activitiesRepo.findTotalDeletedCount(orgId);
    }

    private int getTotalChangedCount(long orgId){
        return activitiesRepo.findTotalChangedCount(orgId);
    }

    private int getTotalMovedCount(long orgId){
        return activitiesRepo.findTotalMovedCount(orgId);
    }

    public List<FileHistoryStatistics> getFileHistoryStatisticsDay(long orgId) {
        List<LocalDateTime> allHours = getLast24Hours(); // 최근 24시간의 시간 단위 리스트
        LocalDateTime startDateTime = allHours.get(0);
        LocalDateTime endDateTime = allHours.get(allHours.size() - 1);

        List<Object[]> results = activitiesRepo.findFileHistoryStatistics(orgId, startDateTime, endDateTime);

        Map<LocalDateTime, FileHistoryStatistics> statisticsMap = new HashMap<>();

        for (Object[] row : results) {
            LocalDateTime timestamp = (LocalDateTime) row[0]; // LocalDateTime으로 직접 처리
            LocalDateTime hour = timestamp.withMinute(0).withSecond(0).withNano(0); // 시간 단위로 정규화
            int uploadCount = ((Number) row[1]).intValue();
            int modifyCount = ((Number) row[2]).intValue();
            int deletedCount = ((Number) row[3]).intValue();
            int movedCount = ((Number) row[4]).intValue();

            FileHistoryStatistics stats = statisticsMap.getOrDefault(hour, FileHistoryStatistics.builder()
                    .date(hour.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .uploadCount(0)
                    .deletedCount(0)
                    .modifyCount(0)
                    .movedCount(0)
                    .build());

            stats.setUploadCount(stats.getUploadCount() + uploadCount);
            stats.setModifyCount(stats.getModifyCount() + modifyCount);
            stats.setDeletedCount(stats.getDeletedCount() + deletedCount);
            stats.setMovedCount(stats.getMovedCount() + movedCount);

            statisticsMap.put(hour, stats);
        }

        return allHours.stream()
                .map(hour -> statisticsMap.getOrDefault(hour, FileHistoryStatistics.builder()
                        .date(hour.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .uploadCount(0)
                        .deletedCount(0)
                        .modifyCount(0)
                        .movedCount(0)
                        .build()))
                .toList();
    }

    private List<LocalDateTime> getLast24Hours() {
        List<LocalDateTime> hours = new ArrayList<>();
        LocalDateTime endDateTime = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startDateTime = endDateTime.minusHours(23);

        while (!startDateTime.isAfter(endDateTime)) {
            hours.add(startDateTime);
            startDateTime = startDateTime.plusHours(1);
        }

        return hours;
    }
}