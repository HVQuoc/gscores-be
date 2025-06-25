package com.gotest.gscore.service;

import com.gotest.gscore.dto.ScoreResponse;
import com.gotest.gscore.dto.StudentScoreResponse;
import com.gotest.gscore.entity.Student;
import com.gotest.gscore.repository.StudentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final StudentRepo studentRepo;

    public StudentScoreResponse getStudentScores(String sbd) {
        Student student = studentRepo.findById(sbd)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<ScoreResponse> scores = student.getScores().stream()
                .map(score -> new ScoreResponse(
                        score.getSubject().getCode(),
                        score.getSubject().getName(),
                        score.getScore()
                ))
                .toList();

        return new StudentScoreResponse(student.getSbd(), student.getMa_ngoai_ngu(), scores);
    }
}
