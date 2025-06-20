ALTER TABLE question
    ADD COLUMN submitted_answer CHAR(1),
    ADD COLUMN answered_at TIMESTAMPTZ;

COMMENT ON COLUMN question.submitted_answer IS 'The answer submitted by the user (A, B, C, or D). NULL if not answered yet.';
COMMENT ON COLUMN question.answered_at IS 'Timestamp when the user submitted their answer. NULL if not answered yet.';
