package com.gotest.gscore.repository;

import com.gotest.gscore.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreRepo extends JpaRepository<Score, Long> {
}

