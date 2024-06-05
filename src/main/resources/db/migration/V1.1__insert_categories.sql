insert into category_entity (id, name)
values (1, 'CAR')
ON CONFLICT (id) DO NOTHING;