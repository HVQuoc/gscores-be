package com.gotest.gscore.service;

import com.gotest.gscore.dto.ScoreRange;
import com.gotest.gscore.dto.ScoreReportResponse;
import com.gotest.gscore.repository.SubjectRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScoreReportService {

    public static final List<ScoreRange> DEFAULT_RANGES = List.of(
            new ScoreRange("Excellent", 8.0, null),
            new ScoreRange("Good", 6.0, 7.99),
            new ScoreRange("Average", 4.0, 5.99),
            new ScoreRange("Weak", null, 3.99)
    );

    private final JdbcTemplate jdbcTemplate;
    private final SubjectRepo subjectRepo;

    public ScoreReportResponse generateReport(String subjectCode, List<ScoreRange> ranges) {

        // Validate subject exists
        if (!subjectExists(subjectCode)) {
            throw new IllegalArgumentException("Subject with code '" + subjectCode + "' does not exist.");
        }

        Map<String, Long> result = new LinkedHashMap<>();;

        for (ScoreRange range : ranges) {
            String sql = buildQuery(subjectCode, range);
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            result.put(range.label(), count.longValue());
        }

        return new ScoreReportResponse(subjectCode, result);
    }

    private boolean subjectExists(String subjectCode) {
        return subjectRepo.existsByCode(subjectCode);
    }

    private String buildQuery(String subjectCode, ScoreRange range) {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM scores WHERE subject_code = ?");
        List<Object> params = new ArrayList<>();
        params.add(subjectCode);

        if (range.min() != null) {
            sb.append(" AND score >= ").append(range.min());
        }
        if (range.max() != null) {
            sb.append(" AND score <= ").append(range.max());
        }

        return sb.toString().replace("?", "'" + subjectCode + "'");
    }
}
