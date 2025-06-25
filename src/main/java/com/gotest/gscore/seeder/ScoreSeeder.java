package com.gotest.gscore.seeder;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.util.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class ScoreSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ScoreSeeder.class);
    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 500;
    private static final String CSV_FILE = "data/diem_thi_thpt_2024.csv";

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Starting score seeder ===");

        long existing = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Long.class);
        if (existing > 0) {
            log.info("Students already exist. Skipping seeding.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(CSV_FILE).getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.warn("CSV is empty");
                return;
            }

            String[] headers = headerLine.split(",");
            List<String> subjectCodes = Arrays.asList(headers);

            insertSubjectsIfNeeded(subjectCodes);

            List<Object[]> studentBatch = new ArrayList<>();
            List<Object[]> scoreBatch = new ArrayList<>();

            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 2) continue;

                String sbd = fields[0];
                String maNgoaiNgu = fields.length >= headers.length ? fields[headers.length - 1] : null;

                studentBatch.add(new Object[]{sbd, maNgoaiNgu});
                for (int i = 1; i < fields.length - 1; i++) {

                    if (fields[i].isEmpty() || fields[i].isBlank()) continue;
                    String scoreStr = fields[i];
                    try {
                        double scoreValue = Double.parseDouble(scoreStr);
                        String subjectCode = headers[i];
                        scoreBatch.add(new Object[]{sbd, subjectCode, scoreValue});
                    } catch (NumberFormatException e) {
                        log.warn("Invalid score '{}' at line {}", scoreStr, lineCount + 2);
                    }

                }

                lineCount++;

                if (lineCount % BATCH_SIZE == 0) {
                    batchInsertStudents(studentBatch);
                    batchInsertScores(scoreBatch);
                    studentBatch.clear();
                    scoreBatch.clear();
                    log.info("Seeded {} lines...", lineCount);
                }
            }

            // Final batch
            if (!studentBatch.isEmpty()) {
                batchInsertStudents(studentBatch);
                batchInsertScores(scoreBatch);
            }

            log.info("Seeder completed: {} lines", lineCount);

        } catch (Exception e) {
            log.error("Seeding failed: {}", e.getMessage(), e);
        }
    }

    private void insertSubjectsIfNeeded(List<String> subjectCodes) {
        log.info("Seeding subjects...");
        for (int i = 1; i < subjectCodes.size() - 1; i++) {
            String code = subjectCodes.get(i);
            jdbcTemplate.update(
                    "INSERT INTO subjects (code, name) VALUES (?, ?) ON CONFLICT (code) DO NOTHING",
                    code, code
            );
        }
    }

    private void batchInsertStudents(List<Object[]> batch) {
        String sql = "INSERT INTO students (sbd, ma_ngoai_ngu) VALUES (?, ?) ON CONFLICT (sbd) DO NOTHING";
        jdbcTemplate.batchUpdate(sql, batch);
    }

    private void batchInsertScores(List<Object[]> batch) {
        String sql = "INSERT INTO scores (sbd, subject_code, score) VALUES (?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, (String) batch.get(i)[0]);
                ps.setString(2, (String) batch.get(i)[1]);
                ps.setDouble(3, (Double) batch.get(i)[2]);
            }

            public int getBatchSize() {
                return batch.size();
            }
        });
    }
}
