package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.FileUploadTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileUploadTableRepo extends JpaRepository<FileUploadTable, Long> {
    @Query("SELECT o.id FROM FileUploadTable fu " +
            "JOIN fu.orgSaaS os " +
            "JOIN os.org o "+
            "WHERE fu.id = :fileId ")
    Optional<Long> findOrgIdByFileId(@Param("fileId") long fileId);
}
