-- Flyway runs this after every migrate, once its lock is released. Its history
-- table lives in public too, so close it to Supabase's Data API like every
-- other table (see V4). Safe to repeat.
alter table if exists public.flyway_schema_history enable row level security;
