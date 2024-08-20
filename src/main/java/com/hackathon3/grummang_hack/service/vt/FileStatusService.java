package com.hackathon3.grummang_hack.service.vt;

import com.hackathon3.grummang_hack.model.dto.FileListDto;
import com.hackathon3.grummang_hack.model.dto.FileStatusDto;
import com.hackathon3.grummang_hack.model.dto.VtReportDto;
import com.hackathon3.grummang_hack.model.entity.FileStatus;
import com.hackathon3.grummang_hack.model.entity.StoredFile;
import com.hackathon3.grummang_hack.model.entity.VtReport;
import com.hackathon3.grummang_hack.repository.StoredFileRepo;
import com.hackathon3.grummang_hack.repository.FileStatusRepo;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class FileStatusService {

    private static final Logger logger = LoggerFactory.getLogger(FileStatusService.class);

    private final StoredFileRepo storedFileRepository;
    private final FileStatusRepo fileStatusRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public FileStatusService(FileStatusRepo fileStatusRepository, ModelMapper modelMapper, StoredFileRepo storedFileRepository){
        this.fileStatusRepository = fileStatusRepository;
        this.modelMapper = modelMapper;
        this.storedFileRepository = storedFileRepository;
    }

    @Transactional
    public void createFileStatus(Long fileId) {
        // StoredFile을 찾습니다. 없으면 예외를 던집니다.
        StoredFile storedFile = storedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("StoredFile not found"));

        // FileStatus 인스턴스를 생성합니다.
        FileStatus fileStatus = FileStatus.builder()
                .storedFile(storedFile)
                .gscanStatus(-1)
                .dlpStatus(-1)
                .vtStatus(-1)
                .build();

        // FileStatus를 저장합니다.
        fileStatusRepository.save(fileStatus);
    }

    @Transactional
    public void updateVtStatus(Long fileId, int status) {
        // 리포트가 성공적으로 저장된 경우 status를 업데이트 해줌
        Optional<FileStatus> optionalFileStatus = fileStatusRepository.findByStoredFileId(fileId);

        optionalFileStatus.ifPresentOrElse(fileStatus -> {
            fileStatus.setVtStatus(status);
            fileStatusRepository.save(fileStatus);
            logger.info("FileStatus updated for fileId: {}", fileId);
        }, () -> {
            logger.error("FileStatus not found for fileId: {}", fileId);
        });
    }

    private VtReportDto convertToVtReportDto(VtReport vtReport) {
        return modelMapper.map(vtReport, VtReportDto.class);
    }

    private FileStatusDto convertToFileStatusDto(FileStatus fileStatus) {
        return modelMapper.map(fileStatus, FileStatusDto.class);
    }

    public int getVtStatusByFileId(Long fileId) {
        return fileStatusRepository.findByStoredFileId(fileId)
                .map(FileStatus::getVtStatus)
                .orElse(-1); // 상태가 없으면 -1 반환
    }

}

