INSERT INTO region_entity (id, name_eng, name_ru, name_hy, country_id)
VALUES
(1,'Aragatsotn', 'Арагацотн', 'Արագածոտն', 1),
(2,'Ararat', 'Арарат', 'Արարատ', 1),
(3,'Armavir', 'Армавир', 'Արմավիր', 1),
(4,'Gegharkunik', 'Гехаркуник', 'Գեղարքունիք', 1),
(5,'Kotayk', 'Котайк', 'Կոտայք', 1),
(6,'Lori', 'Лори', 'Լոռի', 1),
(7,'Shirak', 'Ширак', 'Շիրակ', 1),
(8,'Syunik', 'Сюник', 'Սյունիք', 1),
(9,'Tavush', 'Тавуш', 'Տավուշ', 1),
(10,'Vayots Dzor', 'Вайоц Дзор', 'Վայոց Ձոր', 1),
(11,'Yerevan', 'Ереван', 'Երևան', 1)
ON CONFLICT (id) DO NOTHING;