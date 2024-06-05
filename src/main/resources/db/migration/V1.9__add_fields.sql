INSERT INTO field_entity (id, field_name, is_prefilled, is_required, measurement_id, description_id)
VALUES (1, 'mark', false, true, null, 1),
(2, 'model', false, true, null, 1),
(3, 'generation', false, true, null, 4),
(4, 'exterior color', false, true, null, 3),
(5, 'interior color', false, true, null, 2)
ON CONFLICT (id) DO NOTHING;
