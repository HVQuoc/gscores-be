package com.gotest.gscore.controller;

import com.gotest.gscore.dto.GroupRequest;
import com.gotest.gscore.dto.GroupStudentResponse;
import com.gotest.gscore.dto.ScoreRangeRequest;
import com.gotest.gscore.dto.ScoreReportResponse;
import com.gotest.gscore.service.ScoreReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports/score-level")
@RequiredArgsConstructor
public class ScoreReportController {
    private final ScoreReportService scoreReportService;

    // Default range of scores
    @GetMapping("/{subjectCode}")
    public ResponseEntity<?> getReportBySubjectWithDefaultRange(@PathVariable String subjectCode) {
        try {
            ScoreReportResponse result = scoreReportService.generateReport(subjectCode, ScoreReportService.DEFAULT_RANGES);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Custom range of scores
    @PostMapping("/{subjectCode}")
    public ResponseEntity<?> getReportBySubject(
            @PathVariable String subjectCode,
            @RequestBody ScoreRangeRequest request) {

        try {
            ScoreReportResponse result = scoreReportService.generateReport(subjectCode, request.ranges());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/group")
    public ResponseEntity<List<GroupStudentResponse>> getGroupAStudents(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(scoreReportService.getTopGroupScoreStudents(null, limit));
    }

    @PostMapping("/group")
    public ResponseEntity<List<GroupStudentResponse>> getCustomGroupStudents(
            @RequestBody GroupRequest request,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(scoreReportService.getTopGroupScoreStudents(request.subjects(), limit));
    }
}
