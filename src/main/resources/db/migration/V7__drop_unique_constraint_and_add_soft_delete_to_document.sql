ALTER TABLE document
    DROP CONSTRAINT IF EXISTS document_secondary_file_name_key;

ALTER TABLE document
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN deleted_at TIMESTAMPTZ;

COMMENT ON COLUMN document.secondary_file_name IS 'Secondary filename for the document (no longer unique)';
COMMENT ON COLUMN document.deleted IS 'Soft delete flag';
COMMENT ON COLUMN document.deleted_at IS 'Timestamp when document was soft deleted';