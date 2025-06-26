package com.gotest.gscore.service;

import com.gotest.gscore.dto.GroupStudentResponse;
import com.gotest.gscore.dto.ScoreRange;
import com.gotest.gscore.dto.ScoreReportResponse;
import com.gotest.gscore.repository.SubjectRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreReportService {

    public static final List<ScoreRange> DEFAULT_RANGES = List.of(
            new ScoreRange("Excellent", 8.0, null),
            new ScoreRange("Good", 6.0, 7.99),
            new ScoreRange("Average", 4.0, 5.99),
            new ScoreRange("Weak", null, 3.99)
    );

    public static final List<String> DEFAULT_GROUP = List.of("toan", "vat_li", "hoa_hoc");

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

    public List<GroupStudentResponse> getTopGroupScoreStudents(List<String> subjects, int limit) {
        if (subjects == null || subjects.isEmpty()) subjects = DEFAULT_GROUP;
        validateSubjects(subjects);

        String selectCols = subjects.stream()
                .map(sub -> String.format(
                        "SUM(CASE WHEN subject_code = '%s' THEN score ELSE 0 END) AS %s", sub, sub))
                .collect(Collectors.joining(", "));

        String totalExpr = subjects.stream()
                .map(sub -> String.format("SUM(CASE WHEN subject_code = '%s' THEN score ELSE 0 END)", sub))
                .collect(Collectors.joining(" + "));

        String sql = String.format("""
            SELECT sbd, %s, (%s) AS total_score
            FROM scores
            WHERE subject_code IN (%s)
            GROUP BY sbd
            ORDER BY total_score DESC
            LIMIT ?
        """, selectCols, totalExpr,
                subjects.stream().map(s -> "?").collect(Collectors.joining(", ")));

        List<Object> params = new ArrayList<>(subjects);
        params.add(limit);

        List<String> finalSubjects = subjects;
        int subjectCount = finalSubjects.size();
        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            String sbd = rs.getString("sbd");
            double total = rs.getDouble("total_score");

            Map<String, Double> scoreMap = new LinkedHashMap<>();
            for (String subject : finalSubjects) {
                scoreMap.put(subject, rs.getDouble(subject));
            }

            return new GroupStudentResponse(sbd, total, subjectCount,scoreMap);
        });
    }

    public void validateSubjects(List<String> subjectCodes) {
        if (subjectCodes == null || subjectCodes.isEmpty()) {
            throw new IllegalArgumentException("Subject list cannot be empty");
        }

        String sql = "SELECT code FROM subjects WHERE code IN (:codes)";
        String inSql = String.join(",", Collections.nCopies(subjectCodes.size(), "?"));

        List<String> existing = jdbcTemplate.queryForList(
                "SELECT code FROM subjects WHERE code IN (" + inSql + ")",
                subjectCodes.toArray(),
                String.class
        );

        List<String> notFound = subjectCodes.stream()
                .filter(sub -> !existing.contains(sub))
                .toList();

        if (!notFound.isEmpty()) {
            throw new IllegalArgumentException("Invalid subjects: " + String.join(", ", notFound));
        }
    }
}
