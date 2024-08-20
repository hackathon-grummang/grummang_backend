package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.MonitoredUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MonitoredUsersRepo extends JpaRepository<MonitoredUsers, Long> {
    Optional<MonitoredUsers> findByUserId(String userId);

    boolean existsByUserId(String userId, int orgSaaSId);

    List<Object[]> findTopUsers(int orgId, int saasId);
}
