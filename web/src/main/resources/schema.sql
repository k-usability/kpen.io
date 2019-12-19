CREATE TABLE person (
    id serial primary key,
    name text not null,
    auth0_sub text null,
    email text null,
    is_admin boolean not null default false
);

CREATE TABLE project (
    id serial primary key,
    name text not null,
    user_id int not null,
    s3_bucket text not null,
    s3_key text not null,
    program_filename text not null,
    spec_filename text not null,
    creation_dt timestamp with time zone not null,
    is_compilation_error boolean not null,
    compilation_error_message text null,
    foreign key (user_id) references person (id)
);

CREATE TABLE project_tag (
    id serial primary key ,
    project_id int not null,
    tag text not null,
    unique (project_id, tag)
);

CREATE TABLE job (
    id serial primary key,
    project_id int null,
    benchmark_name text not null,
    spec_name text not null,
    kprove text not null,
    semantics text not null,
    request_dt timestamp with time zone  not null,
    s3_bucket text not null,
    s3_key text not null,
    spec_filename text not null,
    timeout_sec int not null,
    memlimit_mb int not null,
    processing_dt timestamp with time zone,
    output_log_s3_key text,
    error_log_s3_key text,
    status_code int,
    timed_out boolean,
    proved boolean,
    completed_dt timestamp with time zone
);