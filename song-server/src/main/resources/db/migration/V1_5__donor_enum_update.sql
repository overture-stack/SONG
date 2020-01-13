ALTER TYPE gender RENAME TO gender_old_type;
CREATE TYPE gender as ENUM('Male','Female','Other');
ALTER TABLE donor RENAME COLUMN gender TO gender_old;
ALTER TABLE donor ADD COLUMN gender gender;
UPDATE donor SET gender='Male' where gender_old='male';
UPDATE donor SET gender='Female' where gender_old='female';
UPDATE donor SET gender='Other' where gender_old='unspecified';
ALTER TABLE donor DROP COLUMN gender_old;
DROP TYPE gender_old_type CASCADE;
