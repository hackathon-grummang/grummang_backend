package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.Activities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivitiesRepo extends JpaRepository<Activities, Long> {
  
    Optional<Activities> findByEventTsAndEventType(LocalDateTime eventTs, String eventType);

    @Query("SELECT a FROM Activities a WHERE a.saasFileId = :saasFileId AND a.eventType = 'file_upload'")
    Activities getActivitiesBySaaSFileId(@Param("saasFileId") String saasFileId);

    @Query("SELECT a FROM Activities a WHERE a.saasFileId = :saasFileId AND a.eventTs = :timestamp")
    Activities findAllBySaasFileIdAndTimeStamp(@Param("saasFileId") String saasFileId, @Param("timestamp") LocalDateTime timestamp);

    @Query("SELECT a FROM Activities a WHERE a.user.orgSaaS.org.id = :orgId AND a.fileGroup.groupName = :groupName")
    List<Activities> findByOrgIdAndGroupName(@Param("orgId") long orgId, @Param("groupName") String groupName);

    @Query("SELECT a FROM Activities a WHERE a.saasFileId = :saasFileId")
    List<Activities> findListBySaasFileId(@Param("saasFileId") String saasFileId);

    List<Activities> findByUser_OrgSaaS_Org_Id(long orgId);

    @Query("SELECT a FROM Activities a WHERE a.saasFileId IN (SELECT f.saasFileId FROM FileUploadTable f WHERE f.hash = :hash) AND a.eventTs IN (SELECT f.timestamp FROM FileUploadTable f WHERE f.hash = :hash)")
    List<Activities> findByHash(@Param("hash") String hash);

    @Query("SELECT a.user.orgSaaS.org.id FROM Activities a WHERE a.id = :activityId")
    Long findOrgIdByActivityId(@Param("activityId") long id);

    @Query("SELECT a FROM Activities a " +
            "JOIN a.user u " +
            "JOIN u.orgSaaS os " +
            "WHERE os.org.id = :orgId " +
            "AND a.id IN (" +
            "SELECT fg.id FROM FileGroup fg WHERE fg.groupName = :groupName" +
            ")")
    List<Activities> findAllByOrgIdAndGroupName(
            @Param("orgId") long orgId,
            @Param("groupName") String groupName
    );

    @Query("SELECT DATE(av.eventTs) AS date, " +
            "SUM(CASE WHEN av.eventType = 'file_upload' THEN 1 ELSE 0 END) AS uploadCount, " +
            "SUM(CASE WHEN av.eventType = 'file_change' THEN 1 ELSE 0 END) AS modifyCount, " +
            "SUM(CASE WHEN av.eventType = 'file_delete' THEN 1 ELSE 0 END) AS deletedCount, " +
            "SUM(CASE WHEN av.eventType = 'file_move' THEN 1 ELSE 0 END) AS movedCount " +
            "FROM Activities av " +
            "JOIN av.user mu " +
            "JOIN mu.orgSaaS os " +
            "WHERE os.org.id = :orgId AND av.eventTs BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(av.eventTs)")
    List<Object[]> findFileHistoryStatistics(
            @Param("orgId") long orgId,
            @Param("startDate") LocalDateTime startDateTime,
            @Param("endDate") LocalDateTime endDateTime
    );

    @Query("SELECT COUNT(av) " +
            "FROM Activities av " +
            "JOIN av.user mu " +
            "JOIN mu.orgSaaS os " +
            "WHERE os.org.id = :orgId AND av.eventType = 'file_upload'")
    int findTotalUploadCount(@Param("orgId") long orgId);

    @Query("SELECT COUNT(av) " +
            "FROM Activities av " +
            "JOIN av.user mu " +
            "JOIN mu.orgSaaS os " +
            "WHERE os.org.id = :orgId AND av.eventType = 'file_delete'")
    int findTotalDeletedCount(@Param("orgId") long orgId);

    @Query("SELECT COUNT(av) " +
            "FROM Activities av " +
            "JOIN av.user mu " +
            "JOIN mu.orgSaaS os " +
            "WHERE os.org.id = :orgId AND av.eventType = 'file_change'")
    int findTotalChangedCount(@Param("orgId") long orgId);

    @Query("SELECT COUNT(av) " +
            "FROM Activities av " +
            "JOIN av.user mu " +
            "JOIN mu.orgSaaS os " +
            "WHERE os.org.id = :orgId AND av.eventType = 'file_move'")
    int findTotalMovedCount(@Param("orgId") long orgId);

    @Query("SELECT a FROM Activities a WHERE a.eventTs = :eventTs AND a.eventType = :eventType")
    Optional<Activities> findByEventTsAndEventType(@Param("eventTs") LocalDateTime eventTs, @Param("eventType") String eventType);

}
