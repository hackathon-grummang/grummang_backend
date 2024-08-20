package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoredFileRepo extends JpaRepository<StoredFile, Long> {
    Optional<StoredFile> findBySaltedHash(String saltedHash);

    @Query("SELECT SUM(sf.size) FROM Org o " +
            "INNER JOIN OrgSaaS os ON o.id = os.org.id " +
            "INNER JOIN Saas s ON os.saas.id = s.id " +
            "INNER JOIN FileUploadTable fu ON os.id = fu.orgSaaS.id " +
            "INNER JOIN StoredFile sf ON fu.hash = sf.saltedHash " +
            "WHERE o.id = :orgId AND s.id = :saasId")
    Long getTotalFileSize(@Param("orgId") int orgId, @Param("saasId") int saasId);

    @Query("SELECT SUM(sf.size) FROM Org o " +
            "INNER JOIN OrgSaaS os ON o.id = os.org.id " +
            "INNER JOIN Saas s ON os.saas.id = s.id " +
            "INNER JOIN FileUploadTable fu ON os.id = fu.orgSaaS.id " +
            "INNER JOIN StoredFile sf ON fu.hash = sf.saltedHash " +
            "INNER JOIN VtReport vr ON sf.id = vr.storedFile.id " +
            "WHERE o.id = :orgId AND s.id = :saasId AND vr.threatLabel != 'none'")
    Long getTotalMaliciousFileSize(@Param("orgId") int orgId, @Param("saasId") int saasId);

    @Query("SELECT SUM(sf.size) FROM Org o " +
            "INNER JOIN OrgSaaS os ON o.id = os.org.id " +
            "INNER JOIN Saas s ON os.saas.id = s.id " +
            "INNER JOIN FileUploadTable fu ON os.id = fu.orgSaaS.id " +
            "INNER JOIN StoredFile sf ON fu.hash = sf.saltedHash " +
            "INNER JOIN DlpReport dr ON sf.id = dr.storedFile.id " +
            "WHERE o.id = :orgId AND s.id = :saasId AND dr.dlp = true")
    Long getTotalDlpFileSize(@Param("orgId") int orgId, @Param("saasId") int saasId);

    @Query("SELECT sf.savePath FROM StoredFile sf WHERE sf.saltedHash = :saltedHash")
    Optional<String> findSavePathBySaltedHash(@Param("saltedHash") String saltedHash);


    @Query("SELECT COUNT(fu.id) FROM Org o "+
            "INNER JOIN OrgSaaS os ON o.id = os.org.id "+
            "INNER JOIN Saas s ON os.saas.id = s.id "+
            "INNER JOIN FileUploadTable fu ON os.id = fu.orgSaaS.id "+
            "WHERE o.id = :orgId AND s.id = :saasId AND fu.deleted != true")
    int countTotalFiles(@Param("orgId") int orgId, @Param("saasId") int saasId);

    @Query("SELECT COUNT(fu.id) FROM Org o " +
            "INNER JOIN OrgSaaS os ON o.id = os.org.id " +
            "INNER JOIN Saas s ON os.saas.id = s.id " +
            "INNER JOIN FileUploadTable fu ON os.id = fu.orgSaaS.id " +
            "INNER JOIN DlpReport dr ON fu.hash = dr.storedFile.saltedHash " +
            "WHERE o.id = :orgId AND s.id = :saasId AND dr.dlp = true AND fu.deleted != true")
    int countSensitiveFiles(@Param("orgId") int orgId, @Param("saasId") int saasId);

    @Query("SELECT COUNT(fu.id) FROM Org o " +
            "INNER JOIN OrgSaaS os ON o.id = os.org.id " +
            "INNER JOIN Saas s ON os.saas.id = s.id " +
            "INNER JOIN FileUploadTable fu ON os.id = fu.orgSaaS.id " +
            "INNER JOIN VtReport vr ON fu.hash = vr.storedFile.saltedHash " +
            "WHERE o.id = :orgId AND s.id = :saasId AND vr.threatLabel != 'none' AND fu.deleted != true")
    int countMaliciousFiles(@Param("orgId") int orgId, @Param("saasId") int saasId);

    @Query("SELECT COUNT(mu.userId) FROM Org o " +
            "INNER JOIN OrgSaaS os ON o.id = os.org.id " +
            "INNER JOIN Saas s ON os.saas.id = s.id " +
            "INNER JOIN MonitoredUsers mu ON os.id = mu.orgSaaS.id " +
            "WHERE o.id = :orgId AND s.id = :saasId")
    int countConnectedAccounts(@Param("orgId") int orgId, @Param("saasId") int saasId);
}
