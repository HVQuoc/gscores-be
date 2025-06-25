package com.gotest.gscore.dto;

import java.util.Map;

public record ScoreReportResponse(
        String subject,
        Map<String, Long> score_levels
) {}
