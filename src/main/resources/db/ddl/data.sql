-- Reference / seed data for company-registration-api
-- Run after schema.sql. Idempotent on H2 2.x and PostgreSQL (INSERT ... ON CONFLICT).
-- Must stay in sync with RequestStatus enum and dev client keys documented in README.

INSERT INTO registration_request_statuses (code, display_name, sort_order, terminal) VALUES
    ('PENDING', 'Pending', 1, FALSE),
    ('PROCESSING', 'Processing', 2, FALSE),
    ('COMPLETED', 'Completed', 3, TRUE),
    ('FAILED', 'Failed', 4, TRUE)
ON CONFLICT (code) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    sort_order = EXCLUDED.sort_order,
    terminal = EXCLUDED.terminal;

INSERT INTO clients (client_id, api_key, created_at, updated_at) VALUES
    ('dev-client', 'dev-api-key-change-me', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('partner-client', 'partner-api-key-change-me', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('internal-client', 'internal-api-key-change-me', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (client_id) DO NOTHING;
