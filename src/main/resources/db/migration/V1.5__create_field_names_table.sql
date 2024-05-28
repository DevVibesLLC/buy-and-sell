CREATE TABLE if not exists field_name_entity (
     id BIGINT PRIMARY KEY,
     name VARCHAR(255) NOT NULL
);

INSERT INTO field_name_entity (id, name)
VALUES
(1, 'Mark'),
(2, 'Model'),
(3, 'Year'),
(4, 'Mileage')
ON CONFLICT (id) DO NOTHING;