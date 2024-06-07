INSERT INTO field_name_entity (id, field_name, is_prefilled, is_required, measurement_id, description_id)
VALUES
(1, 'Mark', false, true, null, 1),
(2, 'Model', false, true, null, 1),
(3, 'Year', false, true, null, 1),
(4, 'Body Type', false, true, null, 1),
(5, 'Engine Size', false, true, 40, 1),
(6, 'Transmission', false, true, null, 1),
(7, 'Drive Type', false, true, null, 1),

(8, 'Mileage', false, true, 2, 2),
(9, 'Steering Wheel', false, true, null, 2),
(10, 'Cleared Customs', false, true, null, 2),

(11, 'Color', false, true, null, 3),
(12, 'Wheel Size', false, true, 55, 3),
(13, 'Headlights', false, true, null, 3),

(14, 'Interior Color', false, true, null, 4),
(15, 'Interior Material', false, true, null, 4),
(16, 'Sunroof', false, true, null, 4)

ON CONFLICT (id) DO NOTHING;
