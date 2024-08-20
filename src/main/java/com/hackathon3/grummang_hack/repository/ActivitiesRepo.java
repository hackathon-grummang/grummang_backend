package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.Activities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ActivitiesRepo extends JpaRepository<Activities, Long> {
    @Query("SELECT a FROM Activities a WHERE a.eventTs = :eventTs AND a.eventType = :eventType")
    Optional<Activities> findByEventTsAndEventType(@Param("eventTs") LocalDateTime eventTs, @Param("eventType") String eventType);
}
