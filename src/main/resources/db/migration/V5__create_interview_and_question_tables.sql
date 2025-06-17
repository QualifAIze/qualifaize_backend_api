CREATE TYPE difficulty AS ENUM ('EASY', 'MEDIUM', 'HARD');
CREATE TYPE interview_status AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');

CREATE TABLE interview
(
    id                  UUID PRIMARY KEY,
    name                VARCHAR(255)     NOT NULL,
    description         TEXT,
    difficulty          difficulty       NOT NULL DEFAULT 'MEDIUM',
    status              interview_status NOT NULL DEFAULT 'SCHEDULED',
    scheduled_date      TIMESTAMPTZ,
    start_time          TIMESTAMPTZ,
    end_time            TIMESTAMPTZ,
    document_id         UUID             NOT NULL,
    created_by_user_id  UUID             NOT NULL,
    assigned_to_user_id UUID,
    created_at          TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ      NOT NULL DEFAULT now(),

    CONSTRAINT fk_interview_document
        FOREIGN KEY (document_id)
            REFERENCES document (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_interview_created_by_user
        FOREIGN KEY (created_by_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_interview_assigned_to_user
        FOREIGN KEY (assigned_to_user_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);

CREATE TABLE question
(
    id             UUID PRIMARY KEY,
    interview_id   UUID         NOT NULL,
    question_text  TEXT         NOT NULL,
    difficulty     difficulty   NOT NULL DEFAULT 'MEDIUM',
    option_a       TEXT         NOT NULL,
    option_b       TEXT         NOT NULL,
    option_c       TEXT         NOT NULL,
    option_d       TEXT         NOT NULL,
    correct_option CHAR(1)      NOT NULL,
    explanation    TEXT,
    question_order INTEGER      NOT NULL DEFAULT 1,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_question_interview
        FOREIGN KEY (interview_id)
            REFERENCES interview (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_question_correct_option_valid
        CHECK (correct_option IN ('A', 'B', 'C', 'D')),

    CONSTRAINT chk_question_order_positive
        CHECK (question_order > 0),

    CONSTRAINT uk_question_interview_order
        UNIQUE (interview_id, question_order)
);

COMMENT ON TABLE interview IS 'Stores interview sessions based on PDF documents with user assignments';
COMMENT ON TABLE question IS 'Stores multiple choice questions associated with interviews';

COMMENT ON COLUMN interview.document_id IS 'Reference to the PDF document this interview is based on';
COMMENT ON COLUMN question.correct_option IS 'The correct answer option (A, B, C, or D)';
COMMENT ON COLUMN question.question_order IS 'Order of question within the interview (1-based)';