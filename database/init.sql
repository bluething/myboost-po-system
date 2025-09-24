-- Initialize habib_ali_machpud Database
-- This script runs when PostgreSQL container starts for the first time

-- Set timezone to UTC
SET timezone = 'UTC';

-- Create database if not exists (already created by POSTGRES_DB env var)
-- CREATE DATABASE IF NOT EXISTS habib_ali_machpud;

-- Connect to the database
\c habib_ali_machpud;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Set default timezone for the session
SET TIME ZONE 'UTC';

-- Configure PostgreSQL for optimal performance with minimal resources
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET track_activity_query_size = 2048;
ALTER SYSTEM SET pg_stat_statements.track = 'all';
ALTER SYSTEM SET pg_stat_statements.max = 1000;

-- Connection settings
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET idle_in_transaction_session_timeout = '10min';
ALTER SYSTEM SET statement_timeout = '30s';
ALTER SYSTEM SET lock_timeout = '10s';

-- Logging configuration
ALTER SYSTEM SET log_destination = 'stderr';
ALTER SYSTEM SET logging_collector = off;
ALTER SYSTEM SET log_min_messages = 'WARNING';
ALTER SYSTEM SET log_min_error_statement = 'ERROR';
ALTER SYSTEM SET log_min_duration_statement = 1000;  -- Log slow queries (> 1 second)
ALTER SYSTEM SET log_line_prefix = '%t [%p]: [%l-1] user=%u,db=%d,app=%a,client=%h ';
ALTER SYSTEM SET log_timezone = 'UTC';

-- Security settings
ALTER SYSTEM SET ssl = off;  -- Disable SSL for development
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';

-- Apply settings (will take effect after restart, but some are already applied via command line)
SELECT pg_reload_conf();

-- Grant necessary permissions to the user
GRANT ALL PRIVILEGES ON DATABASE habib_ali_machpud TO po_user;
GRANT ALL ON SCHEMA public TO po_user;

-- Create a health check function
CREATE OR REPLACE FUNCTION health_check()
RETURNS TEXT AS $$
BEGIN
RETURN 'Database is healthy at ' || NOW()::TEXT;
END;
$$ LANGUAGE plpgsql;

-- Grant execute permission on health check function
GRANT EXECUTE ON FUNCTION health_check() TO po_user;

-- Show current configuration
SELECT name, setting, unit, short_desc
FROM pg_settings
WHERE name IN (
               'max_connections',
               'shared_buffers',
               'effective_cache_size',
               'maintenance_work_mem',
               'work_mem',
               'timezone'
    )
ORDER BY name;

-- Log successful initialization
DO $$
BEGIN
    RAISE NOTICE 'Database habib_ali_machpud initialized successfully at %', NOW();
    RAISE NOTICE 'User po_user granted all privileges';
    RAISE NOTICE 'Extensions created: uuid-ossp, pg_stat_statements';
    RAISE NOTICE 'Timezone set to UTC: %', CURRENT_SETTING('timezone');
END $$;