DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE tablename = 'scores'
          AND indexname = 'unique_student_subject'
    ) THEN
ALTER TABLE scores
    ADD CONSTRAINT unique_student_subject UNIQUE (sbd, subject_code);
END IF;
END
$$;