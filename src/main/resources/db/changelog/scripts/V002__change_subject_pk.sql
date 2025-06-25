-- Drop existing primary key if it exists (assumes it's on 'id')
ALTER TABLE subjects DROP CONSTRAINT IF EXISTS subjects_pkey;

-- Make 'code' the new primary key
ALTER TABLE subjects ADD CONSTRAINT pk_subjects_code PRIMARY KEY (code);