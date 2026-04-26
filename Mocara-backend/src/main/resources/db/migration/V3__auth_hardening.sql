alter table refresh_tokens
  add column if not exists device_id varchar(128),
  add column if not exists ip_address varchar(64),
  add column if not exists user_agent varchar(512),
  add column if not exists created_at timestamptz not null default now(),
  add column if not exists expires_at timestamptz,
  add column if not exists revoked_at timestamptz,
  add column if not exists replaced_by_token_hash varchar(128);

update refresh_tokens
set created_at = to_timestamp(created_at_ms / 1000.0)
where created_at is null;

update refresh_tokens
set expires_at = to_timestamp(expires_at_ms / 1000.0)
where expires_at is null;

create index if not exists ix_refresh_tokens_device_id on refresh_tokens(device_id);
