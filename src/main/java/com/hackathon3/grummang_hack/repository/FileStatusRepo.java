package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileStatusRepo extends JpaRepository<FileStatus, Integer> {
    List<FileStatus> findByVtStatus(int vtStatus);
    Optional<FileStatus> findByStoredFileId(Long fileId);
}
