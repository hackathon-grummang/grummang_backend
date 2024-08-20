package com.hackathon3.grummang_hack.repository;

import com.hackathon3.grummang_hack.model.entity.AdminUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUsersRepo extends JpaRepository<AdminUsers, Integer> {
    Optional<AdminUsers> findByEmail(String email);
}
