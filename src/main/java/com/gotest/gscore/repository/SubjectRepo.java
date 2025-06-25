package com.gotest.gscore.repository;

import com.gotest.gscore.entity.Student;
import com.gotest.gscore.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRepo extends JpaRepository<Subject, String> {
    boolean existsByCode(String code);
}
