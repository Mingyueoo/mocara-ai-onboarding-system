create table if not exists protocols (
  id              varchar(100) primary key,
  drug_id         varchar(64)  not null,
  drug_name       varchar(255) not null,
  intent          varchar(32)  not null,
  description     text         not null default ''
);

create unique index if not exists ux_protocols_drug_intent on protocols(drug_id, intent);

create table if not exists protocol_steps (
  id                      bigserial primary key,
  protocol_id             varchar(100) not null references protocols(id) on delete cascade,
  step_number             int not null,
  title                   varchar(255) not null,
  content                 text not null,
  step_type               varchar(32) not null,
  requires_confirmation   boolean not null default false,
  options_json            jsonb null,
  confirmation_items_json jsonb null
);

create unique index if not exists ux_protocol_steps_protocol_stepnum
  on protocol_steps(protocol_id, step_number);

create table if not exists patient_sessions (
  session_id    uuid primary key,
  drug_id       varchar(64) not null,
  protocol_id   varchar(100) not null references protocols(id),
  current_step  int not null default 0,
  is_completed  boolean not null default false,
  is_escalated  boolean not null default false,
  start_time_ms bigint not null
);

create table if not exists session_responses (
  id            bigserial primary key,
  session_id    uuid not null references patient_sessions(session_id) on delete cascade,
  step_number   int not null,
  response      text not null,
  created_at_ms bigint not null
);

create unique index if not exists ux_session_responses_session_step
  on session_responses(session_id, step_number);

create table if not exists chat_messages (
  id            uuid primary key,
  session_id     uuid not null references patient_sessions(session_id) on delete cascade,
  role          varchar(32) not null,
  content       text not null,
  timestamp_ms  bigint not null,
  is_escalated  boolean not null default false,
  avatar_emotion varchar(32) not null default 'NEUTRAL'
);

create index if not exists ix_chat_messages_session_ts
  on chat_messages(session_id, timestamp_ms);

create table if not exists escalations (
  id               bigserial primary key,
  session_id       uuid unique not null references patient_sessions(session_id) on delete cascade,
  reason           text not null,
  level            varchar(32) not null,
  timestamp_ms     bigint not null,
  contact_required boolean not null default true,
  urgency          varchar(255) not null default '',
  instructions     text not null default ''
);
