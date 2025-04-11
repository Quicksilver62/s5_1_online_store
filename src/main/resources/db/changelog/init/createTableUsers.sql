create schema if not exists store;

create table if not exists store.users
(
    id              uuid primary key default gen_random_uuid(),
    email           varchar,
    cart            integer
);