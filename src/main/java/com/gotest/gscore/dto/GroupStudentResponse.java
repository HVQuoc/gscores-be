package com.gotest.gscore.dto;

import java.util.Map;

public record GroupStudentResponse(
        String sbd,
        double total_score,
        int subject_count,
        Map<String, Double> subject_scores
) {}