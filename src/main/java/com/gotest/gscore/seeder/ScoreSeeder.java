package com.gotest.gscore.seeder;

import com.gotest.gscore.entity.Score;
import com.gotest.gscore.entity.Student;
import com.gotest.gscore.entity.Subject;
import com.gotest.gscore.repository.ScoreRepo;
import com.gotest.gscore.repository.StudentRepo;
import com.gotest.gscore.repository.SubjectRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ScoreSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ScoreSeeder.class);

    private final StudentRepo studentRepo;
    private final SubjectRepo subjectRepo;
    private final ScoreRepo scoreRepo;

    private static final int BATCH_SIZE = 100;
    private static final String CSV_PATH = "src/main/resources/data/diem_thi_thpt_2024.csv";


    @Override
    public void run(String... args) throws Exception {
        log.info("Checking if seeding is required...");
        if (studentRepo.count() > 0 && subjectRepo.count() > 0) {
            log.info("Seeding skipped: student data already exists.");
            return;
        }

        log.info("Seeding student scores from CSV...");
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_PATH))) {
            String headerLine = br.readLine(); // read header
            if (headerLine == null) {
                log.warn("CSV is empty. Aborting.");
            }

            String[] headers = headerLine.split(",");

            // Create subjects (from header, skipping sbd and ma_ngoai_ngu columns)
            Map<String, Subject> subjectMap = createSubjectsFromHeader(headers);

            List<Student> students = new ArrayList<>();
            List<Score> scores = new ArrayList<>();

            int lineCount = 0;
            String line;

            while ((line = br.readLine()) != null) {
                lineCount++;
                String[] fields = line.split(",");
                if (fields.length < 2) continue;

                // Create student
                Student student = new Student();
                student.setSbd(fields[0]);
                student.setMa_ngoai_ngu(fields.length > headers.length - 1 ? fields[headers.length - 1] : null);
                students.add(student);

                // Create scores for subjects
                for (int i = 1; i < fields.length - 1; i++) {
                    String scoreStr = fields[i];
                    if (scoreStr != null && !scoreStr.isEmpty()) {
                        try {
                            double scoreValue = Double.parseDouble(scoreStr);
                            Score score = new Score();
                            score.setStudent(student);
                            score.setSubject(subjectMap.get(headers[i]));
                            score.setScore(scoreValue);
                            scores.add(score);
                        } catch (NumberFormatException e) {
                            int lineNumber = lineCount + 2; //skip header line + 1's based count
                            log.warn("Invalid score '{}', line {}", fields[i], lineNumber);
                        }
                    }
                }

                // Batch insert every <n = BATCH_SIZE> lines
                if (lineCount % BATCH_SIZE == 0) {
                    persistBatch(students, scores);
                    students.clear();
                    scores.clear();
                }
            }

            // Save remaining
            if (!students.isEmpty()) {
                persistBatch(students, scores);
            }

            log.info("Seeding completed. Total students: {}", lineCount);
        } catch(Exception e) {
            log.error("Seeding failed: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void persistBatch(List<Student> students, List<Score> scores) {
        studentRepo.saveAll(students);
        scoreRepo.saveAll(scores);
    }

    private Map<String, Subject> createSubjectsFromHeader(String[] headers) {
        Map<String, Subject> map = new HashMap<>();
        for (int i = 1; i < headers.length - 1; i++) {
            Subject subject = new Subject();
            subject.setCode(headers[i]);
            subject.setName(headers[i]);
            Subject savedSubject = subjectRepo.save(subject);
            map.put(headers[i], savedSubject);
            log.info("Created subject '{}'", savedSubject.getCode());
        }
        return map;
    }
}
