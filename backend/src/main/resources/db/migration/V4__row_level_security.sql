-- Supabase publishes every table in the public schema through its Data API,
-- reachable with the project's anon key, which is meant to be public. Turning
-- on row level security with no policies means that API can read or write
-- nothing. The backend is unaffected: it connects as the role that owns these
-- tables, and table owners are not subject to row level security.
--
-- Flyway's own history table is skipped here because Flyway holds a lock on it
-- while migrations run; afterMigrate.sql covers it once the lock is released.
--
-- Every table added in a later migration must do the same; the RLS test in
-- the backend test suite fails CI if one is missed.

do $$
declare
    t record;
begin
    for t in select tablename from pg_tables where schemaname = 'public'
                       and tablename <> 'flyway_schema_history' loop
        execute format('alter table public.%I enable row level security', t.tablename);
    end loop;
end
$$;
