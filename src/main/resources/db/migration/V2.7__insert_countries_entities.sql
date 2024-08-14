INSERT INTO country_entity (id, name_eng, name_ru, name_hy)
VALUES
(1,'Armenia', 'Армения', 'Հայաստան'),
(2,'Another Country', 'Другая Страна', 'Այլ Երկիր')
ON CONFLICT (id) DO NOTHING;