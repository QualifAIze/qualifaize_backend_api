ALTER TABLE interview
    ADD COLUMN candidate_review TEXT;

COMMENT ON COLUMN interview.candidate_review IS 'Review about the candidate after completion.';