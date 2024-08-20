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

    Optional<FileUploadTable> findByTimestampAndHash(LocalDateTime event_ts, String hash);

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

    Optional<Object> findOrgIdByHash(Long fileId);
}
