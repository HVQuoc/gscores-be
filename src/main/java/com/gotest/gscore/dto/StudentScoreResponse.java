package com.gotest.gscore.dto;

import java.util.List;

public record StudentScoreResponse(String sbd, String ma_ngoai_ngu, List<ScoreResponse> scores) {
}
