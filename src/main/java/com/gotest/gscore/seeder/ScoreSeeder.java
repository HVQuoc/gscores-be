package com.gotest.gscore.seeder;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

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
    private static final String PROGRESS_FILE = "seeder_progress.log";
    private static final String ERROR_FILE = "seeder_errors.log";

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Starting score seeder ===");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(CSV_FILE).getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter errorWriter = new BufferedWriter(new FileWriter(ERROR_FILE, true))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.warn("CSV is empty");
                return;
            }

            String[] headers = headerLine.split(",");
            List<String> subjectCodes = Arrays.asList(headers);

            insertSubjectsIfNeeded(subjectCodes);

            int resumeFromLine = readProgress();
            log.info("Resuming from line {}", resumeFromLine);

            List<Object[]> studentBatch = new ArrayList<>();
            List<Object[]> scoreBatch = new ArrayList<>();

            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount <= resumeFromLine) continue; // Skip processed lines

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
                        errorWriter.write("NumberFormatException -  " + lineCount + ": " + line);
                        errorWriter.newLine();
                    }

                }

                if (lineCount % BATCH_SIZE == 0) {
                    insertSafe(studentBatch, scoreBatch, lineCount);
                    studentBatch.clear();
                    scoreBatch.clear();
                }
            }

            // Final batch
            if (!studentBatch.isEmpty()) {
                insertSafe(studentBatch, scoreBatch, lineCount);
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

    private void insertSafe(List<Object[]> students, List<Object[]> scores, int currentLine) {
        try {
            batchInsertStudents(students);
            try {
                batchInsertScores(scores);
            } catch (Exception e) {
                log.error("Failed to insert scores batch at line {}: {}", currentLine, e.getMessage());
                retryInsertScoresIndividually(scores);
            }
            writeProgress(currentLine);
            log.info("Inserted batch ending at line {}", currentLine);
        } catch (Exception ex) {
            log.error("Failed to insert batch ending at line {}: {}", currentLine, ex.getMessage());
        }
    }
    private int readProgress() {
        try {
            File f = new File(PROGRESS_FILE);
            if (!f.exists()) return 0;
            try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                return Integer.parseInt(r.readLine());
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private void writeProgress(int lineNumber) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(PROGRESS_FILE))) {
            w.write(String.valueOf(lineNumber));
        } catch (IOException e) {
            log.warn("Failed to write progress file: {}", e.getMessage());
        }
    }

    private void retryInsertScoresIndividually(List<Object[]> scores) {
        String sql = "INSERT INTO scores (sbd, subject_code, score) VALUES (?, ?, ?)";
        for (Object[] row : scores) {
            try {
                jdbcTemplate.update(sql, row);
            } catch (Exception ex) {
                log.warn("Failed to insert score: sbd={}, subject={}, score={}. Error: {}",
                        row[0], row[1], row[2], ex.getMessage());
            }
        }
    }

}
