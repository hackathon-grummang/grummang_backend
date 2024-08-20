package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.FileUploadTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface FileUploadTableRepo extends JpaRepository<FileUploadTable, Long> {

    Optional<FileUploadTable> findByTimestampAndHash(LocalDateTime event_ts, String hash);
}
