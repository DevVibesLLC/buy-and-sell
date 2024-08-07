INSERT INTO region_entity (id, name, country_id)
VALUES
(1,'Aragatsotn',1),
(2,'Ararat',1),
(3,'Armavir',1),
(4,'Gegharkunik',1),
(5,'Kotayk',1),
(6,'Lori',1),
(7,'Shirak',1),
(8,'Syunik',1),
(9,'Tavush',1),
(10,'Vayots',1),
(11,'Yerevan',1)
ON CONFLICT (id) DO NOTHING;