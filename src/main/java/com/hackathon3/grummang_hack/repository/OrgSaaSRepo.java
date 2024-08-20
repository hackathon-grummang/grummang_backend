package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.OrgSaaS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgSaaSRepo extends JpaRepository<OrgSaaS, Integer> {

    List<OrgSaaS> findBySpaceId(String temp);
}
