INSERT INTO field_name_entity (id, field_name, is_prefilled, is_required, measurement_id, description_id)
VALUES
(1, 'Mark', false, true, null, 1),
(2, 'Model', false, true, null, 1),
(3, 'Year', false, true, null, 1),
(4, 'Body Type', false, true, null, 1),
(5, 'Engine',false,true,null,1),
(6, 'Engine Size', false, true, 40, 1),
(7, 'Transmission', false, true, null, 1),
(8, 'Drive Type', false, true, null, 1),

(9, 'Mileage', false, true, 2, 2),
(10, 'Steering Wheel', false, true, null, 2),
(11, 'Cleared Customs', false, true, null, 2),

(12, 'Color', false, true, null, 3),
(13, 'Wheel Size', false, true, 55, 3),
(14, 'Headlights', false, true, null, 3),

(15, 'Interior Color', false, true, null, 4),
(16, 'Interior Material', false, true, null, 4),
(17, 'Sunroof', false, true, null, 4),

(18, 'Mark', false, true, null, 5),
(19, 'Model', false, true, null, 5),
(20, 'Year', false, true, null, 5),
(21, 'Engine Type', false, true, null, 5),
(22, 'Transmission', false, true, null, 5),
(23, 'Chassis configuration',false, true, null,5),

(24, 'Mileage', false, true, 2, 6),
(25, 'Steering Wheel', false, true, null, 6),
(26, 'Cleared Customs', false, true, null, 6),
(27, 'Color', false, true, null, 6)

ON CONFLICT (id) DO NOTHING;
