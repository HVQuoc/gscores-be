-- STUDENTS
CREATE TABLE students (
                          sbd VARCHAR(20) PRIMARY KEY,
                          ma_ngoai_ngu VARCHAR(10)
);

-- SUBJECTS
CREATE TABLE subjects (
                          id SERIAL PRIMARY KEY,
                          code VARCHAR(50) UNIQUE NOT NULL,
                          name VARCHAR(100)
);

-- SCORES
CREATE TABLE scores (
                        id SERIAL PRIMARY KEY,
                        sbd VARCHAR(20) NOT NULL,
                        subject_code VARCHAR(50) NOT NULL,
                        score DOUBLE PRECISION,
                        CONSTRAINT fk_student FOREIGN KEY (sbd) REFERENCES students(sbd),
                        CONSTRAINT fk_subject FOREIGN KEY (subject_code) REFERENCES subjects(code)
);
