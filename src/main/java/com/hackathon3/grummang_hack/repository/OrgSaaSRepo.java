package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.OrgSaaS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrgSaaSRepo extends JpaRepository<OrgSaaS, Integer> {

    @Query("SELECT os.spaceId FROM OrgSaaS os WHERE os.id = :spaceId")
    String getSpaceID(@Param("spaceId")int workspaceId);

    Optional<OrgSaaS> findBySpaceId(String spaceId);
}
