package com.maxminiapp.repository;

import com.maxminiapp.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByMaxUserId(Long maxUserId);

    List<AppUser> findByAdminTrueOrderByCreatedAtDesc();

    boolean existsByMaxUserId(Long maxUserId);
}
