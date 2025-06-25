package com.gotest.gscore.controller;

import com.gotest.gscore.dto.ScoreRangeRequest;
import com.gotest.gscore.dto.ScoreReportResponse;
import com.gotest.gscore.service.ScoreReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports/score-level")
@RequiredArgsConstructor
public class ScoreReportController {
    private final ScoreReportService reportService;

    // Default range of scores
    @GetMapping("/{subjectCode}")
    public ResponseEntity<?> getReportBySubjectWithDefaultRange(@PathVariable String subjectCode) {
        try {
            ScoreReportResponse result = reportService.generateReport(subjectCode, ScoreReportService.DEFAULT_RANGES);
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
            ScoreReportResponse result = reportService.generateReport(subjectCode, request.ranges());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
