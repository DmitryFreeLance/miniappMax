package com.maxminiapp.repository;

import com.maxminiapp.model.InfoPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InfoPostRepository extends JpaRepository<InfoPost, Long> {
    List<InfoPost> findAllByOrderByCreatedAtDesc();
}
