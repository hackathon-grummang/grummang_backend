package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.dto.slack.file.SlackRecentFileDto;
import com.hackathon3.grummang_hack.model.entity.FileUploadTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileUploadTableRepo extends JpaRepository<FileUploadTable, Long> {
    @Query("SELECT o.id FROM FileUploadTable fu " +
            "JOIN fu.orgSaaS os " +
            "JOIN os.org o "+
            "WHERE fu.id = :fileId ")
    Optional<Long> findOrgIdByFileId(@Param("fileId") long fileId);
    @Query("SELECT f FROM FileUploadTable f WHERE f.timestamp = :timestamp AND f.hash = :hash")
    Optional<FileUploadTable> findByTimestampAndHash(@Param("timestamp") LocalDateTime timestamp, @Param("hash") String hash);

    @Query("SELECT SlackRecentFileDTO(a.fileName, u.userName, sf.type, fu.timestamp) " +
            "FROM FileUploadTable fu " +
            "JOIN OrgSaaS os ON fu.orgSaaS.id = os.id " +
            "JOIN Activities a ON fu.saasFileId = a.saasFileId " +
            "JOIN StoredFile sf ON fu.hash = sf.saltedHash " +
            "JOIN MonitoredUsers u ON a.user.id = u.id " +
            "WHERE os.org.id = :orgId AND os.saas.id = :saasId " +
            "AND a.eventType = 'file_upload' " +  // 조건 추가
            "ORDER BY fu.timestamp DESC LIMIT 10")
    List<SlackRecentFileDto> findRecentFilesByOrgIdAndSaasId(@Param("orgId") int orgId, @Param("saasId") int saasId);
}
