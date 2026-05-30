-- Tear-down script for company-registration-api
-- Compatible with H2 2.x and PostgreSQL. Drops tables in dependency order (children first).
-- Partial indexes (uk_companies_name_active, idx_registration_requests_pending_reg_created) are removed with their tables.
-- To wipe rows but keep tables, use clean-data.sql instead.
DROP TABLE IF EXISTS registration_request_status_history;
DROP TABLE IF EXISTS registration_requests;
DROP TABLE IF EXISTS company_name_history;
DROP TABLE IF EXISTS companies;
DROP TABLE IF EXISTS registration_request_statuses;
DROP TABLE IF EXISTS clients;
