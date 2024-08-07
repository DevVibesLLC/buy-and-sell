INSERT INTO city_entity (id, name, region_id)
VALUES
(1,'Nor Nork',11),
(2,'Arabkir',11),
(3,'Kentron',11),
(4,'Shengavit',11),
(5,'Erebuni',11)
ON CONFLICT (id) DO NOTHING;