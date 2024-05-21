create table if not exists items_for_sell
(
    price       double precision not null,
    quantity    integer,
    category_id bigint           not null
        constraint KF_category
            references categories,
    created_at  timestamp(6),
    id          bigint           not null
        primary key,
    updated_at  timestamp(6),
    user_id     bigint
        unique
        constraint FK_user
            references users,
    description varchar(255)     not null,
    name        varchar(255)     not null
);