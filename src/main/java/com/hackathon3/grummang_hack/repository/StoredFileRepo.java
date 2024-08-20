package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoredFileRepo extends JpaRepository<StoredFile, Long> {

    Optional<StoredFile> findBySaltedHash(String hash);

    Long getTotalDlpFileSize(int orgId, int saasId);

    Long getTotalMaliciousFileSize(int orgId, int saasId);

    Long getTotalFileSize(int orgId, int saasId);

    int countTotalFiles(int orgId, int saasId);

    int countSensitiveFiles(int orgId, int saasId);

    int countMaliciousFiles(int orgId, int saasId);

    int countConnectedAccounts(int orgId, int saasId);
}
