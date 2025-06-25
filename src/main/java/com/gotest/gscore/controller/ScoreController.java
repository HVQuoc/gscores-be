package com.gotest.gscore.controller;

import com.gotest.gscore.dto.StudentScoreResponse;
import com.gotest.gscore.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @GetMapping("/{sbd}")
    public ResponseEntity<StudentScoreResponse> getScoreBySbd(@PathVariable String sbd) {
        return ResponseEntity.ok(scoreService.getStudentScores(sbd));
    }


}
