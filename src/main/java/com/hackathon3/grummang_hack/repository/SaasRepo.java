package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.Saas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaasRepo extends JpaRepository<Saas, Integer> {


    Optional<Saas> findBySaasName(String saasName);
}
