INSERT INTO description_entity (id, header, category_id)
VALUES (1, 'SPECIFICATIONS', 1),
(2, 'ADDITIONAL_INFORMATION', 1),
(3, 'EXTERIOR', 1),
(4, 'INTERIOR', 1)
ON CONFLICT (id) DO NOTHING;