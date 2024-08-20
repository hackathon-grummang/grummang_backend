package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoredFileRepo extends JpaRepository<StoredFile, Long> {
    Optional<StoredFile> findBySaltedHash(String hash);
}
