INSERT INTO country_entity (id, name)
VALUES
(1,'Armenia')
ON CONFLICT (id) DO NOTHING;