create table if not exists users (
  id              bigserial primary key,
  email           varchar(255) not null unique,
  password_hash   varchar(255) not null,
  enabled         boolean not null default true,
  created_at_ms   bigint not null
);

create table if not exists user_roles (
  user_id         bigint not null references users(id) on delete cascade,
  role            varchar(32) not null,
  primary key (user_id, role)
);

create table if not exists refresh_tokens (
  id              bigserial primary key,
  user_id         bigint not null references users(id) on delete cascade,
  token_hash      varchar(128) not null unique,
  expires_at_ms   bigint not null,
  revoked         boolean not null default false,
  created_at_ms   bigint not null
);

create index if not exists ix_refresh_tokens_user_id on refresh_tokens(user_id);

alter table patient_sessions
  add column if not exists user_id bigint references users(id);

create index if not exists ix_patient_sessions_user_id on patient_sessions(user_id);
