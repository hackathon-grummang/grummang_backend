package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.FileGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FileGroupRepo extends JpaRepository<FileGroup, Long> {

    @Query("SELECT fg.groupName FROM FileGroup fg WHERE fg.id = :activityId")
    String findGroupNameById(@Param("activityId") long activityId);

    @Query("SELECT fg.groupType FROM FileGroup fg WHERE fg.id = :activityId")
    String findGroupTypeById(@Param("activityId") long activityId);

}
