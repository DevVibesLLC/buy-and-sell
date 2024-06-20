INSERT INTO notebook_mark_entity (id, name, category_id)
VALUES
(1,'', 1)
ON CONFLICT (id) DO NOTHING;