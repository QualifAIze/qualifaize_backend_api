ALTER TABLE document
    ADD COLUMN uploaded_by_user_id UUID;

ALTER TABLE document
    ADD CONSTRAINT fk_document_uploaded_by_user
        FOREIGN KEY (uploaded_by_user_id)
            REFERENCES users (id)
            ON DELETE NO ACTION;

COMMENT ON COLUMN document.uploaded_by_user_id IS 'References the user who uploaded this document';