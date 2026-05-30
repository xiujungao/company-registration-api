-- Remove all rows from application tables (schema stays intact).
-- Compatible with H2 2.x and PostgreSQL 10+.
-- Deletes in foreign-key order (children first).
-- To restore reference data (clients, statuses), run db/ddl/data.sql afterward.
-- To remove tables entirely, use db/ddl/drop.sql instead.

DELETE FROM registration_request_status_history;
DELETE FROM registration_requests;
DELETE FROM company_name_history;
DELETE FROM companies;
DELETE FROM registration_request_statuses;
DELETE FROM clients;
