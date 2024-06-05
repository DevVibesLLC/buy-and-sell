INSERT INTO description_entity (id, header, category_id)
VALUES (1, 'SPECIFICATIONS', 1),
(2, 'EXTERIOR', 1),
(3, 'INTERIOR', 1),
(4, 'ADDITIONAL_INFORMATION', 1)
ON CONFLICT (id) DO NOTHING;