package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.Gscan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GscanRepo extends JpaRepository<Gscan, Integer> {
}
