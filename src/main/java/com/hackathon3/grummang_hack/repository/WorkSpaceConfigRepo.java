package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.WorkspaceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkSpaceConfigRepo extends JpaRepository<WorkspaceConfig, String> {
    Optional<WorkspaceConfig> findById(int id);
    boolean existsById(int id);
    List<WorkspaceConfig> findByIdIn(List<Integer> configIds);
}
