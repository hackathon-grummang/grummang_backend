package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.Activities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ActivitiesRepo extends JpaRepository<Activities, Long> {

    Optional<Activities> findByEventTsAndEventType(LocalDateTime eventTs, String eventType);
}
