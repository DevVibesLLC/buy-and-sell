create table users
(
    id                         bigint       not null
        primary key,
    created_at                 timestamp(6),
    email                      varchar(255) not null
        constraint UK_email
            unique,
    is_account_non_expired     boolean,
    is_account_non_locked      boolean,
    is_credentials_non_expired boolean,
    is_enabled                 boolean,
    is_verified                boolean,
    name                       varchar(255),
    password                   varchar(255) not null,
    second_name                varchar(255),
    updated_at                 timestamp(6),
    verification_code          varchar(255),
    role_id                    integer      not null
        constraint FK_roles
            references roles
);