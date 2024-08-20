package com.hackathon3.grummang_hack.service.file;

import com.hackathon3.grummang_hack.model.dto.file.FileDashboardDto;
import com.hackathon3.grummang_hack.model.dto.file.StatisticsDto;
import com.hackathon3.grummang_hack.model.dto.file.TotalTypeDto;
import com.hackathon3.grummang_hack.repository.FileUploadTableRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FileBoardReturnService {
    private final FileUploadTableRepo fileUploadRepo;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public FileBoardReturnService(FileUploadTableRepo fileUploadRepo){
        this.fileUploadRepo = fileUploadRepo;
    }

    public FileDashboardDto boardListReturn(long orgId) {

        long totalCount = totalFilesCount(orgId);
        long totalVolume = totalFileSizeCount(orgId);
        int totalMalware = totalMalwareCount(orgId);

        List<TotalTypeDto> totalType = getFileTypeDistribution(orgId);
        List<StatisticsDto> statistics = getFileStatisticsLast24Hours(orgId);

        // FileDashboardDto 객체를 생성하고 반환
        return FileDashboardDto.builder()
                .totalCount(totalCount)
                .totalVolume(totalVolume)
                .totalMalware(totalMalware)
                .totalType(totalType != null ? totalType : Collections.emptyList())
                .statistics(statistics != null ? statistics : Collections.emptyList())
                .build();
    }

    private long totalFilesCount(long orgId) {
        // null을 처리하여 기본값 0L을 반환
        Long count = fileUploadRepo.countFileByOrgId(orgId);
        return count != null ? count : 0L;
    }

    private long totalFileSizeCount(long orgId) {
        // null을 처리하여 기본값 0L을 반환
        Long totalSize = fileUploadRepo.getTotalSizeByOrgId(orgId);
        return totalSize != null ? totalSize : 0L;
    }

    private int totalMalwareCount(long orgId) {
        // null을 처리하여 기본값 0을 반환
        Integer countVtMalware = fileUploadRepo.countVtMalwareByOrgId(orgId);
        return (countVtMalware != null ? countVtMalware : 0);

    }

    private List<TotalTypeDto> getFileTypeDistribution(long orgId) {
        // null을 처리하여 기본값 빈 리스트를 반환
        List<TotalTypeDto> totalType = fileUploadRepo.findFileTypeDistributionByOrgId(orgId);
        return totalType != null ? totalType : Collections.emptyList();
    }

    public List<StatisticsDto> getFileStatisticsMonth(long orgId) {
        List<LocalDate> allDates = getLast30Days();
        LocalDateTime startDateTime = allDates.get(0).atStartOfDay();
        LocalDateTime endDateTime = allDates.get(allDates.size() - 1).atTime(LocalTime.MAX);

        List<Object[]> results = fileUploadRepo.findStatistics(orgId, startDateTime, endDateTime);

        Map<LocalDate, StatisticsDto> statisticsMap = new HashMap<>();

        for (Object[] row : results) {
            LocalDateTime timestamp = (LocalDateTime) row[0];
            LocalDate date = timestamp.toLocalDate();
            long totalSizeInBytes = ((Number) row[1]).longValue();
            long fileCount = ((Number) row[2]).longValue();

            StatisticsDto dto = statisticsMap.getOrDefault(date, new StatisticsDto(
                    date.format(dateFormatter),
                    0,
                    0
            ));

            dto.setCount(dto.getCount() + (int) fileCount);
            dto.setVolume(dto.getVolume() + totalSizeInBytes);

            statisticsMap.put(date, dto);
        }

        return allDates.stream()
                .map(date -> statisticsMap.getOrDefault(date, new StatisticsDto(
                        date.format(dateFormatter),
                        0,
                        0
                )))
                .toList();
    }

    public List<LocalDate> getLast30Days() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);

        while (!startDate.isAfter(endDate)) {
            dates.add(startDate);
            startDate = startDate.plusDays(1);
        }

        return dates;
    }

    public List<StatisticsDto> getFileStatisticsLast24Hours(long orgId) {
        List<LocalDateTime> allHours = getLast24Hours();
        LocalDateTime startDateTime = allHours.get(0);
        LocalDateTime endDateTime = allHours.get(allHours.size() - 1);

        List<Object[]> results = fileUploadRepo.findStatistics(orgId, startDateTime, endDateTime);

        Map<LocalDateTime, StatisticsDto> statisticsMap = new HashMap<>();

        for (Object[] row : results) {
            LocalDateTime timestamp = (LocalDateTime) row[0];
            LocalDateTime hour = timestamp.withMinute(0).withSecond(0).withNano(0); // 시간 단위로 정규화
            long totalSizeInBytes = ((Number) row[1]).longValue();
            long fileCount = ((Number) row[2]).longValue();

            StatisticsDto dto = statisticsMap.getOrDefault(hour, new StatisticsDto(
                    hour.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    0,
                    0
            ));

            dto.setCount(dto.getCount() + (int) fileCount);
            dto.setVolume(dto.getVolume() + totalSizeInBytes);

            statisticsMap.put(hour, dto);
        }

        return allHours.stream()
                .map(hour -> statisticsMap.getOrDefault(hour, new StatisticsDto(
                        hour.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        0,
                        0
                )))
                .toList();
    }

    public List<LocalDateTime> getLast24Hours() {
        List<LocalDateTime> hours = new ArrayList<>();
        LocalDateTime endDateTime = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0); // 현재 시간을 시간 단위로 정규화
        LocalDateTime startDateTime = endDateTime.minusHours(23); // 24시간 전부터 현재까지

        while (!startDateTime.isAfter(endDateTime)) {
            hours.add(startDateTime);
            startDateTime = startDateTime.plusHours(1);
        }

        return hours;
    }


}

