CREATE TABLE document
(
    id                  UUID PRIMARY KEY,
    file_name           VARCHAR(255) NOT NULL,
    secondary_file_name VARCHAR(255) NOT NULL UNIQUE,
    subsections_count   INTEGER      NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ           DEFAULT now()
);

CREATE TABLE subsection
(
    id          UUID PRIMARY KEY,
    document_id UUID    NOT NULL REFERENCES document (id) ON DELETE CASCADE,
    parent_id   UUID REFERENCES subsection (id) ON DELETE CASCADE,
    title       VARCHAR(255),
    content     TEXT    NOT NULL,
    position    INTEGER NOT NULL CHECK (position >= 0),
    level       INTEGER NOT NULL CHECK (level >= 0),
    created_at  TIMESTAMPTZ DEFAULT now()
);