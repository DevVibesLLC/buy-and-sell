-- V1__Create_auto_tables.sql

CREATE TABLE if not exists auto_mark_entity
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE if not exists auto_model_entity
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    auto_mark_id BIGINT       NOT NULL,
    CONSTRAINT fk_auto_mark
        FOREIGN KEY (auto_mark_id)
            REFERENCES auto_mark_entity (id)
);

CREATE TABLE if not exists generation_entity
(
    id                SERIAL PRIMARY KEY,
    generation_number INT    NOT NULL,
    auto_model_id     BIGINT NOT NULL,
    CONSTRAINT fk_auto_model
        FOREIGN KEY (auto_model_id)
            REFERENCES auto_model_entity (id)
);

CREATE TABLE if not exists generation_item_entity
(
    id            SERIAL PRIMARY KEY,
    restyling     INT    NOT NULL,
    year_start    INT    NOT NULL,
    year_end      INT,
    generation_id BIGINT NOT NULL,
    CONSTRAINT fk_generation
        FOREIGN KEY (generation_id)
            REFERENCES generation_entity (id)
);

CREATE TABLE if not exists generation_item_frames_entity
(
    generation_item_id BIGINT       NOT NULL,
    frame              VARCHAR(255) NOT NULL,
    CONSTRAINT fk_generation_item
        FOREIGN KEY (generation_item_id)
            REFERENCES generation_item_entity (id)
);
