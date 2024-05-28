create table if not exists roles
(
    id   bigint
        primary key,
    role varchar(255) not null
        constraint roles_role_check unique
);

INSERT INTO roles (id, role)
VALUES (1,'ROLE_USER'),(2,'ROLE_ADMIN')
ON CONFLICT (id) DO NOTHING;
