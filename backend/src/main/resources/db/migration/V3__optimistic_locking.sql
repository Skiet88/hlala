-- Version columns for optimistic locking on the rows that more than one person
-- can change at the same time: the deposit hold (admins, tenant, landlord),
-- the dispute (evidence and resolution) and the lease (two signatures).
-- Hibernate bumps the number on every update and refuses a write made from a
-- stale read, so concurrent changes fail loudly instead of overwriting each other.

alter table deposit_hold add column version bigint not null default 0;
alter table dispute      add column version bigint not null default 0;
alter table lease        add column version bigint not null default 0;
