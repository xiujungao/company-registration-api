-- Test reference data (empty in-memory H2; plain INSERT — no ON CONFLICT needed).
-- Keep in sync with src/main/resources/db/ddl/data.sql

INSERT INTO registration_request_statuses (code, display_name, sort_order, terminal) VALUES
    ('PENDING', 'Pending', 1, FALSE),
    ('PROCESSING', 'Processing', 2, FALSE),
    ('COMPLETED', 'Completed', 3, TRUE),
    ('FAILED', 'Failed', 4, TRUE);

INSERT INTO clients (client_id, api_key, created_at, updated_at) VALUES
    ('dev-client', 'dev-api-key-change-me', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('partner-client', 'partner-api-key-change-me', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('internal-client', 'internal-api-key-change-me', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
