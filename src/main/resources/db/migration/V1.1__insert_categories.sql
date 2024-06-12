insert into category_entity (id, name)
values
(1, 'CAR'),
(2, 'TRUCK'),
(3, 'BUS')
ON CONFLICT (id) DO NOTHING;