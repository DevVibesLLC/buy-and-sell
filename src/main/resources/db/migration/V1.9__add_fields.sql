INSERT INTO field_name_entity (id, field_name_eng, field_name_ru, field_name_hy, is_prefilled, is_required, measurement_id, description_id)
VALUES

/*CAR*/
(1, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 1),
(2, 'Model', 'Модель', 'Մոդել', false, true, null, 1),
(3, 'Year', 'Год', 'Տարեթիվ', false, true, null, 1),
(4, 'Body Type', 'Тип Кузова', 'Թափքի Տեսակ', false, true, null, 1),
(5, 'Engine Type', 'Тип Двигателя', 'Շարժիչի Տեսակ',false,true,null,1),
(6, 'Engine Size', 'Объём Двигателя', 'Շարժիչի Ծավալ', false, true, 40, 1),
(7, 'Transmission', 'Коробка Передач', 'Փոխանցման Տուփ', false, true, null, 1),
(8, 'Drive Type', 'Привод', 'Քարշակ', false, true, null, 1),

(9, 'Mileage', 'Пробег', 'Վազք', false, true, 2, 2),
(10, 'Steering Wheel', 'Руль', 'Ղեկ', false, true, null, 2),
(11, 'Cleared Customs', 'Растаможка', 'Մաքսազերծում', false, true, null, 2),

(12, 'Color', 'Цвет', 'Գույն', false, true, null, 3),
(13, 'Wheel Size', 'Размер Колес', 'Անիվի Չափս', false, true, 55, 3),
(14, 'Headlights', 'Фары', 'Լուսարձակներ', false, true, null, 3),

(15, 'Interior Color', 'Цвет Салона', 'Սրահի Գույն', false, true, null, 4),
(16, 'Interior Material', 'Материал Салона', 'Սրահ', false, true, null, 4),
(17, 'Sunroof', 'Люк', 'Լյուկ', false, true, null, 4),

/*TRUCK*/
(18, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 5),
(19, 'Model', 'Модель', 'Մոդել', false, true, null, 5),
(20, 'Year', 'Год', 'Տարեթիվ', false, true, null, 5),
(21, 'Engine Type', 'Тип Двигателя', 'Շարժիչի Տեսակ', false, true, null, 5),
(22, 'Transmission', 'Коробка Передач', 'Փոխանցման Տուփ', false, true, null, 5),
(23, 'Chassis Configuration', 'Колесная Формула', 'Շասիի Կոնֆիգուրացիա',false, true, null,5),

(24, 'Mileage', 'Пробег', 'Վազք', false, true, 2, 6),
(25, 'Steering Wheel', 'Руль', 'Ղեկ', false, true, null, 6),
(26, 'Cleared Customs', 'Растаможка', 'Մաքսազերծում', false, true, null, 6),
(27, 'Color', 'Цвет', 'Գույն', false, true, null, 6),

/*BUS*/
(28, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 7),
(29, 'Model', 'Модель', 'Մոդել', false, true, null, 7),
(30, 'Year', 'Год', 'Տարեթիվ', false, true, null, 7),
(31, 'Engine Type', 'Тип Двигателя', 'Շարժիչի Տեսակ', false, true, null, 7),
(32, 'Transmission', 'Коробка Передач', 'Փոխանցման Տուփ', false, true, null, 7),

(33, 'Mileage', 'Пробег', 'Վազք', false, true, 2, 8),
(34, 'Steering Wheel', 'Руль', 'Ղեկ', false, true, null, 8),
(35, 'Cleared Customs', 'Растаможка', 'Մաքսազերծում', false, true, null, 8),
(36, 'Color', 'Цвет', 'Գույն', false, true, null, 8),

/*APARTMENT BUY*/
(37, 'Construction Type', 'Тип Здания', 'Շինության Տիպ', false, true, null, 9),
(38, 'New Construction', 'Новостройка', 'Նորակառույց', false, true, null, 9),
(39, 'Elevator', 'Лифт', 'Վերելակ', false, true, null, 9),
(40, 'Floors in the Building', 'Этажей в Доме', 'Հարկերի Քանակ', false, true, null, 9),
(41, 'The House Has', 'У Дома Есть', 'Շենքն Ունի', false, true, null, 9),
(42, 'Parking', 'Парковка', 'Կայանատեղի', false, true, null, 9),

(43, 'Floor Area', 'Общая площадь', 'Ընդհանուր Մակերես', false, true, 31, 10),
(44, 'Number of Rooms', 'Количество Комнат', 'Սենյակների Քանակ', false, true, null, 10),
(45, 'Number of Bathrooms', 'Количество Санузлов', 'Սանհանգույցների Քանակ', false, true, null, 10),
(46, 'Ceiling Height', 'Высота Потолков', 'Առաստաղի Բարձրություն', false, true, 1, 10),
(47, 'Floor', 'Этаж', 'Հարկ', false, true, null, 10),
(48, 'Balcony', 'Балкон', 'Պատշգամբ', false, true, null, 10),
(49, 'Furniture', 'Мебель', 'Կահույք', false, true, null, 10),
(50, 'Renovation', 'Ремонт', 'Վերանորոգում', false, true, null, 10),
(51, 'Appliances', 'Бытовая Техника', 'Կենցաղային Տեխնիկա', false, true, null, 10),
(52, 'Window Views', 'Виды из Окон', 'Տեսարաններ Պատուհաններից', false, true, null, 10),

/*APARTMENT RENTAL*/
(53, 'Construction Type', 'Тип Здания', 'Շինության Տիպ', false, true, null, 11),
(54, 'New Construction', 'Новостройка', 'Նորակառույց', false, true, null, 11),
(55, 'Elevator', 'Лифт', 'Վերելակ', false, true, null, 11),
(56, 'Floors in the Building', 'Этажей в Доме', 'Հարկերի Քանակ', false, true, null, 11),
(57, 'The House Has', 'У Дома Есть', 'Շենքն Ունի', false, true, null, 11),
(58, 'Parking', 'Парковка', 'Կայանատեղի', false, true, null, 11),

(59, 'Floor Area', 'Общая площадь', 'Ընդհանուր Մակերես', false, true, 31, 12),
(60, 'Number of Rooms', 'Количество Комнат', 'Սենյակների Քանակ', false, true, null, 12),
(61, 'Number of Bathrooms', 'Количество Санузлов', 'Սանհանգույցների Քանակ', false, true, null, 12),
(62, 'Ceiling Height', 'Высота Потолков', 'Առաստաղի Բարձրություն', false, true, 1, 12),
(63, 'Floor', 'Этаж', 'Հարկ', false, true, null, 12),
(64, 'Balcony', 'Балкон', 'Պատշգամբ', false, true, null, 12),
(65, 'Furniture', 'Мебель', 'Կահույք', false, true, null, 12),
(66, 'Renovation', 'Ремонт', 'Վերանորոգում', false, true, null, 12),
(67, 'Amenities', 'Удобства', 'Հարմարություն', false, true, null, 12),
(68, 'Appliances', 'Бытовая Техника', 'Կենցաղային Տեխնիկա', false, true, null, 12),
(69, 'Window Views', 'Виды из Окон', 'Տեսարաններ Պատուհաններից', false, true, null, 12),

(70, 'With Children', 'Можно с Детьми', 'Երեխաների Հետ', false, true, null, 13),
(71, 'With Pets', 'Можно с Животными', 'Կենդանիների Հետ', false, true, null, 13),

(72, 'Utility Payments', 'Коммунальные Платежи', 'Կոմունալ Վճարումներ', false, true, null, 14),
(73, 'Prepayment', 'Предоплата', 'Կանխավճար', false, true, null, 14),

/*HOUSE BUY*/
(74, 'Type', 'Тип', 'Տեսակ', false, true, null,15),
(75, 'Condition', 'Состояние', 'Վիճակ', false, true, null,15),
(76, 'Construction Type', 'Тип Здания', 'Շինության Տիպ', false, true, null,15),
(77, 'House Area', 'Площадь Дома', 'Տան Մակերես', false, true, 31, 15),
(78, 'Floors in the Building', 'Этажей в Доме', 'Հարկերի Քանակ', false, true, null,15),
(79, 'Number of Rooms', 'Количество Комнат', 'Սենյակների Քանակ', false, true, null,15),
(80, 'Number of Bathrooms', 'Количество Санузлов', 'Սանհանգույցների Քանակ', false, true, null,15),
(81, 'Furniture', 'Мебель', 'Կահույք', false, true, null,15),
(82, 'Garage', 'Гараж', 'Ավտոտնակ', false, true, null,15),
(83, 'Renovation', 'Ремонт', 'Վերանորոգում', false, true, null,15),
(84, 'Facilities', 'Удобства', 'Հարմարություն', false, true, null,15),
(85, 'Appliances', 'Бытовая Техника', 'Կենցաղային Տեխնիկա', false, true, null,15),
(86, 'Service Lines', 'Коммуникации', 'Կոմունիկացիաներ', false, true, null,15),

(87, 'Land Area', 'Площадь Участка', 'Հողատարածքի Մակերես', false, true, 31, 16),

/*HOUSE RENTAL*/
(88, 'Type', 'Тип', 'Տեսակ',false,true,null,17),
(89, 'Construction Type', 'Тип Здания', 'Շինության Տիպ',false,true,null,17),
(90, 'House Area', 'Площадь Дома', 'Տան Մակերես',false,true,31,17),
(91, 'Floors in the Building', 'Этажей в Доме', 'Հարկերի Քանակ',false,true,null,17),
(92, 'Number of Rooms', 'Количество Комнат', 'Սենյակների Քանակ',false,true,null,17),
(93, 'Number of Bathrooms', 'Количество Санузлов', 'Սանհանգույցների Քանակ',false,true,null,17),
(94, 'Furniture', 'Мебель', 'Կահույք',false,true,null,17),
(95, 'Garage', 'Гараж', 'Ավտոտնակ',false,true,null,17),
(96, 'Renovation', 'Ремонт', 'Վերանորոգում',false,true,null,17),
(97, 'Amenities', 'Удобства', 'Հարմարություն',false,true,null,17),
(98, 'Appliances', 'Бытовая Техника', 'Կենցաղային Տեխնիկա',false,true,null,17),
(99, 'Service Lines', 'Коммуникации', 'Կոմունիկացիաներ',false,true,null,17),

(100, 'Land Area', 'Площадь Участка', 'Հողատարածքի Մակերես', false, true, 31, 18),

(101, 'With Children', 'Можно с Детьми', 'Երեխաների Հետ', false, true, null, 19),
(102, 'With Pets', 'Можно с Животными', 'Կենդանիների Հետ', false, true, null, 19),

(103, 'Utility Payments', 'Коммунальные Платежи', 'Կոմունալ Վճարումներ', false, true, null, 20),
(104, 'Prepayment', 'Предоплата', 'Կանխավճար', false, true, null, 20),

/*COMMERCIAL BUY*/
(105, 'Type', 'Тип', 'Տեսակ',false,true,null,21),
(106, 'Construction Type', 'Тип Здания', 'Շինության Տիպ',false,true,null,21),
(107, 'Floor Area', 'Общая площадь', 'Ընդհանուր Մակերես',false,true,31,21),
(108, 'Furniture', 'Мебель', 'Կահույք',false,true,null,21),
(109, 'Elevator', 'Лифт', 'Վերելակ',false,true,null,21),
(110, 'Location from the Street', 'Расположение от Улицы', 'Գտնվելու Վայրը Փողոցից',false,true,null,21),
(111, 'Entrance', 'Вход', 'Մուտք',false,true,null,21),
(112, 'Parking', 'Парковка', 'Կայանատեղի',false,true,null,21),

/*COMMERCIAL RENTAL*/
(113, 'Type', 'Тип', 'Տեսակ',false,true,null,22),
(114, 'Floor Area', 'Общая площадь', 'Ընդհանուր Մակերես',false,true,31,22),
(115, 'Furniture', 'Мебель', 'Կահույք',false,true,null,22),
(116, 'Elevator', 'Лифт', 'Վերելակ',false,true,null,22),
(117, 'Location from the Street', 'Расположение от Улицы', 'Գտնվելու Վայրը Փողոցից',false,true,null,22),
(118, 'Entrance', 'Вход', 'Մուտք',false,true,null,22),
(119, 'Parking', 'Парковка', 'Կայանատեղի',false,true,null,22),

(120, 'Lease Type', 'Тип Аренды', 'Վարձակալության Տեսակ', false, true, null,23),
(121, 'Minimum Rental Period', 'Минимальный Срок Аренды', 'Վարձակալության Նվազագույն Ժամկետ', false, true, null,23),
(122, 'Utility Payments', 'Коммунальные Платежи', 'Կոմունալ Վճարումներ', false, true, null, 23),
(123, 'Prepayment', 'Предоплата', 'Կանխավճար', false, true, null, 23),

/*GARAGE AND PARKING BUY*/
(124, 'Type', 'Тип', 'Տեսակ',false,true,null,24),
(125, 'Floor Area', 'Общая площадь', 'Ընդհանուր Մակերես',false,true,31,24),
(126, 'Utilities', 'Коммунальные Услуги', 'Կոմունալ Ծառայություններ',false,true,null,24),
(127, 'Amenities', 'Удобства', 'Հարմարություն',false,true,null,24),

/*GARAGE AND PARKING RENTAL*/
(128, 'Type', 'Тип', 'Տեսակ',false,true,null,25),
(129, 'Floor Area', 'Общая площадь', 'Ընդհանուր Մակերես',false,true,31,25),
(130, 'Utilities', 'Коммунальные Услуги', 'Կոմունալ Ծառայություններ',false,true,null,25),
(131, 'Amenities', 'Удобства', 'Հարմարություն',false,true,null,25),

(132, 'Prepayment', 'Предоплата', 'Կանխավճար', false, true, null, 26),

/*LAND BUY*/
(133, 'Type', 'Тип', 'Տեսակ', false,true,null, 27),
(134, 'Land Area', 'Площадь Участка', 'Հողատարածքի Մակերես', false,true,null, 27),
(135, 'Service Lines', 'Коммуникации', 'Կոմունիկացիաներ', false,true,null, 27),

/*LAND RENTAL*/
(136, 'Type', 'Тип', 'Տեսակ', false,true,null, 28),
(137, 'Land Area', 'Площадь Участка', 'Հողատարածքի Մակերես', false,true,null, 28),
(138, 'Service Lines', 'Коммуникации', 'Կոմունիկացիաներ', false,true,null, 28),

(139, 'Prepayment', 'Предоплата', 'Կանխավճար', false, true, null, 29),

/*NEW CONSTRUCTION APARTMENT*/
(140, 'Construction Type', 'Тип Здания', 'Շինության Տիպ', false, true, null, 30),
(141, 'Elevator', 'Лифт', 'Վերելակ', false, true, null, 30),
(142, 'Floors in the Building', 'Этажей в Доме', 'Հարկերի Քանակ', false, true, null, 30),
(143, 'The House Has', 'У Дома Есть', 'Շենքն Ունի', false, true, null, 30),
(144, 'Parking', 'Парковка', 'Կայանատեղի', false, true, null, 30),

(145, 'Floor Area', 'Общая площадь', 'Ընդհանուր Մակերես', false, true, 31, 31),
(146, 'Number of Rooms', 'Количество Комнат', 'Սենյակների Քանակ', false, true, null, 31),
(147, 'Number of Bathrooms', 'Количество Санузлов', 'Սանհանգույցների Քանակ', false, true, null, 31),
(148, 'Ceiling Height', 'Высота Потолков', 'Առաստաղի Բարձրություն', false, true, 1, 31),
(149, 'Floor', 'Этаж', 'Հարկ', false, true, null, 31),
(150, 'Balcony', 'Балкон', 'Պատշգամբ', false, true, null, 31),

(151, 'Interior Finishing', 'Отделка', 'Հարդարում', false, true, null, 32),
(152, 'Handover Date', 'Срок Сдачи', 'Վերջնաժամկետ', false, true, null, 32),

(153, 'Mortgage is Possible', 'Возможна Ипотека', 'Հնարավոր է Հիփոթեք', false, true, null, 33),

/*NEW CONSTRUCTION HOUSE*/
(154, 'Type', 'Тип', 'Տեսակ', false, true, null, 34),
(155, 'Construction Type', 'Тип Здания', 'Շինության Տիպ', false, true, null, 34),
(156, 'House Area', 'Площадь Дома', 'Տան Մակերես', false, true, null, 34),
(157, 'Floors in the Building', 'Этажей в Доме', 'Հարկերի Քանակ', false, true, null, 34),
(158, 'Number of Rooms', 'Количество Комнат', 'Սենյակների Քանակ', false, true, null, 34),
(159, 'Number of Bathrooms', 'Количество Санузлов', 'Սանհանգույցների Քանակ', false, true, null, 34),
(160, 'Garage', 'Гараж', 'Ավտոտնակ', false, true, null, 34),
(161, 'Service Lines', 'Коммуникации', 'Կոմունիկացիաներ', false, true, null, 34),

(162, 'Land Area', 'Площадь Участка', 'Հողատարածքի Մակերես', false, true, null, 35),

(163, 'Interior Finishing', 'Отделка', 'Հարդարում', false, true, null, 36),
(164, 'Handover Date', 'Срок Сдачи', 'Վերջնաժամկետ', false, true, null, 36),

(165, 'Mortgage is Possible', 'Возможна Ипотека', 'Հնարավոր է Հիփոթեք', false, true, null, 37),

/*APARTMENT DAILY RENTAL*/
(166, 'Construction Type', 'Тип Здания', 'Շինության Տիպ', false, true, null, 38),
(167, 'New Construction', 'Новостройка', 'Նորակառույց', false, true, null, 38),
(168, 'Elevator', 'Лифт', 'Վերելակ', false, true, null, 38),
(169, 'Floors in the Building', 'Этажей в Доме', 'Հարկերի Քանակ', false, true, null, 38),
(170, 'The House Has', 'У Дома Есть', 'Շենքն Ունի', false, true, null, 38),
(171, 'Parking', 'Парковка', 'Կայանատեղի', false, true, null, 38),

(172, 'Floor Area', 'Общая площадь', 'Ընդհանուր Մակերես', false, true, 31, 39),
(173, 'Number of Rooms', 'Количество Комнат', 'Սենյակների Քանակ', false, true, null, 39),
(174, 'Number of Bathrooms', 'Количество Санузлов', 'Սանհանգույցների Քանակ', false, true, null, 39),
(175, 'Ceiling Height', 'Высота Потолков', 'Առաստաղի Բարձրություն', false, true, 1, 39),
(176, 'Floor', 'Этаж', 'Հարկ', false, true, null, 39),
(177, 'Balcony', 'Балкон', 'Պատշգամբ', false, true, null, 39),
(178, 'Renovation', 'Ремонт', 'Վերանորոգում', false, true, null, 39),
(179, 'Comfort', 'Комфорт', 'Կոմֆորտ', false, true, null, 39),
(180, 'Amenities', 'Удобства', 'Հարմարություն', false, true, null, 39),
(181, 'Appliances', 'Бытовая Техника', 'Կենցաղային Տեխնիկա', false, true, null, 39),
(182, 'Window Views', 'Виды из Окон', 'Տեսարաններ Պատուհաններից', false, true, null, 39),

(183, 'Number of Guests', 'Kоличество Гостей', 'Հյուրերի Քանակ', false, true, null, 40),
(184, 'With Children', 'Можно с Детьми', 'Երեխաների Հետ', false, true, null, 40),
(185, 'With Pets', 'Можно с Животными', 'Կենդանիների Հետ', false, true, null, 40),

/*HOUSE DAILY RENTAL*/
(186, 'Type', 'Тип', 'Տեսակ', false, true, null, 41),
(187, 'Construction Type', 'Тип Здания', 'Շինության Տիպ', false, true, null, 41),
(188, 'House Area', 'Площадь Дома', 'Տան Մակերես', false, true, null, 41),
(189, 'Floors in the Building', 'Этажей в Доме', 'Հարկերի Քանակ', false, true, null, 41),
(190, 'Number of Rooms', 'Количество Комнат', 'Սենյակների Քանակ', false, true, null, 41),
(191, 'Number of Bathrooms', 'Количество Санузлов', 'Սանհանգույցների Քանակ', false, true, null, 41),
(192, 'Garage', 'Гараж', 'Ավտոտնակ', false, true, null, 41),
(193, 'Renovation', 'Ремонт', 'Վերանորոգում', false, true, null, 41),
(194, 'Comfort', 'Комфорт', 'Կոմֆորտ', false, true, null, 41),
(195, 'Amenities', 'Удобства', 'Հարմարություն', false, true, null, 41),
(196, 'Appliances', 'Бытовая Техника', 'Կենցաղային Տեխնիկա', false, true, null, 41),

(197, 'Land Area', 'Площадь Участка', 'Հողատարածքի Մակերես', false, true, null, 42),

(198, 'Number of Guests', 'Kоличество Гостей', 'Հյուրերի Քանակ', false, true, null, 43),
(199, 'With Children', 'Можно с Детьми', 'Երեխաների Հետ', false, true, null, 43),
(200, 'With Pets', 'Можно с Животными', 'Կենդանիների Հետ', false, true, null, 43),

/*MOBILE PHONE*/
(201, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 44),
(202, 'Model', 'Модель', 'Մոդել', false, true, null, 44),
(203, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 44),
(204, 'Memory (RAM)', 'Оперативная Память', 'Օպերատիվ Հիշողություն', false, true, null, 44),
(205, 'Memory', 'Встроенная Память', 'Ներքին Հիշողություն', false, true, null, 44),
(206, 'Color', 'Цвет', 'Գույն', false, true, null, 44),

/*NOTEBOOK*/
(207, 'Mark', 'Марка', 'Մակնիշ',false,true,null,45),
(208, 'Condition', 'Состояние', 'Վիճակ',false,true,null,45),
(209, 'Processor', 'Процессор', 'Պրոցեսոր', false, true, null, 45),
(210, 'Memory (RAM)', 'Оперативная Память', 'Օպերատիվ Հիշողություն', false, true, null, 45),
(211, 'Memory', 'Встроенная Память', 'Ներքին Հիշողություն', false, true, null, 45),
(212, 'Screen Resolution', 'Разрешение Экрана', 'էկրանի Լուծաչափ',false,true,null,45),
(213, 'Screen Size', 'Размер экрана', 'Էկրանի Չափ',false,true,10,45),

/*COMPUTER*/
(214, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 46),
(215, 'Processor', 'Процессор', 'Պրոցեսոր', false, true, null, 46),
(216, 'Memory (RAM)', 'Оперативная Память', 'Օպերատիվ Հիշողություն', false, true, null, 46),
(217, 'Memory', 'Встроенная Память', 'Ներքին Հիշողություն', false, true, null, 46),
(218, 'Screen Resolution', 'Разрешение Экрана', 'էկրանի Լուծաչափ', false, true, null, 46),
(219, 'Screen Size', 'Размер экрана', 'Էկրանի Չափ',false,true,10,46),

/*SMART WATCH*/
(220, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 47),
(221, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 47),
(222, 'Color', 'Цвет', 'Գույն', false, true, null, 47),

/*TABLET*/
(223, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 48),
(224, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 48),
(225, 'Screen Size', 'Размер экрана', 'Էկրանի Չափ',false,true,10,48),
(226, 'Memory', 'Встроенная Память', 'Ներքին Հիշողություն', false, true, null, 48),
(227, 'Color', 'Цвет', 'Գույն', false, true, null, 48),

/*TV*/
(228, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 49),
(229, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 49),
(230, 'Screen Size', 'Размер экрана', 'Էկրանի Չափ',false,true, 10, 49),

/*GAMING CONSOLE*/
(231, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 50),
(232, 'Type', 'Тип', 'Տեսակ', false, true, null, 50),
(233, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 50),

/*HEADPHONE*/
(234, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 51),
(235, 'Connection Type', 'Тип Подключения', 'Միացման Տեսակ', false, true, null, 51),
(236, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 51),
(237, 'Color', 'Цвет', 'Գույն', false, true, null, 51),

/*COMPUTER AND NOTEBOOK PARTS*/
(238, 'Type', 'Тип', 'Տեսակ', false, true, null, 52),
(239, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 52),

/*PHOTO AND VIDEO CAMERA*/
(240, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 53),
(241, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 53),

/*COMPUTER GAMES*/
(242, 'Type', 'Тип', 'Տեսակ', false, true, null, 54),
(243, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 54),

/*SMART HOME ACCESSORIES*/
(244, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 55),
(245, 'Type', 'Тип', 'Տեսակ', false, true, null, 55),
(246, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 55),

/*WASHER*/
(247, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 56),
(248, 'Type', 'Тип', 'Տեսակ', false, true, null, 56),
(249, 'Maximum Laundry Capacity', 'Максимальная Загрузка Белья', 'Լվացքի Բեռնման Առավելագույն Չափ', false, true, 11, 56),
(250, 'Laundry Load Type', 'Тип Загрузки Белья', 'Լվացքի Բեռնման Տեսակ', false, true, null, 56),
(251, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 56),

/*CLOTHES DRYER*/
(252, 'Condition', 'Состояние', 'Վիճակ',false, true, null, 57),

/*IRON AND ACCESSORIES*/
(253, 'Type', 'Тип', 'Տեսակ', false, true, null, 58),
(254, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 58),

/*REFRIGERATOR*/
(255, 'Mark', 'Марка', 'Մակնիշ', false, true, null, 59),
(256, 'Type', 'Тип', 'Տեսակ', false, true, null, 59),
(257, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 59),

/*FREEZER*/
(258, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 60),

/*DISHWASHER*/
(259, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 61),

/*MICROWAVE*/
(260, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 62),

/*STOVE*/
(261, 'Type', 'Тип', 'Տեսակ', false, true, null, 63),
(262, 'Burner Type', 'Тип Конфорок', 'Այրիչի Տեսակ', false, true, null, 63),
(263, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 63),

/*COFFEE MAKER AND ACCESSORIES*/
(264, 'Type', 'Тип', 'Տեսակ', false, true, null, 64),
(265, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 64),

/*KETTLES*/
(266, 'Type', 'Тип', 'Տեսակ', false, true, null, 65),
(267, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 65),

/*RANGE_HOODS*/
(268, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 66),

/*VACUUM_CLEANERS*/
(269, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 67),

/*ROBOTIC_VACUUMS*/
(270, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 68),

/*FLOOR_WASHERS*/
(271, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 69),

/*AIR_CONDITIONERS*/
(272, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 70),

/*WATER_HEATERS*/
(273, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 71),

/*AIR_PURIFIERS_AND_HUMIDIFIERS*/
(274, 'Type', 'Тип', 'Տեսակ', false, true, null, 72),
(275, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 72),

/*COMPUTERS_PERIPHERALS*/
(276, 'Type', 'Тип', 'Տեսակ', false, true, null, 73),
(277, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 73),

/*AUDIO_PLAYERS_AND_STEREOS*/
(278, 'Type', 'Тип', 'Տեսակ', false, true, null, 74),

(279, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 75),

/*QUADCOPTERS_AND_DRONES*/
(280, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 76),

/*SOFAS_AND_ARMCHAIRS*/
(281, 'Type', 'Тип', 'Տեսակ', false, true, null, 77),
(282, 'Upholstery', 'Обивка', 'Մակերեսի Ծածկույթ', false, true, null, 77),
(283, 'Color', 'Цвет', 'Գույն', false, true, null, 77),
(284, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 77),

/*STORAGE*/
(285, 'Type', 'Тип', 'Տեսակ', false, true, null, 78),
(286, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 78),

/*TABLES_AND_CHAIRS*/
(287, 'Type', 'Тип', 'Տեսակ', false, true, null, 79),
(288, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 79),

/*BEDROOM_FURNITURE*/
(289, 'Type', 'Тип', 'Տեսակ', false, true, null, 80),
(290, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 80),

/*KITCHEN_FURNITURE*/
(291, 'Type', 'Тип', 'Տեսակ', false, true, null, 81),
(292, 'Color', 'Цвет', 'Գույն', false, true, null, 81),
(293, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 81),

/*GARDEN_FURNITURE*/
(294, 'Type', 'Тип', 'Տեսակ', false, true, null, 82),
(295, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 82),

/*BARBECUE_AND_ACCESSORIES*/
(296, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 83),

/*GARDEN_DECOR*/
(297, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 84),

/*GARDEN_ACCESSORIES*/
(298, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 85),

/*LIGHTING*/
(299, 'Type', 'Тип', 'Տեսակ', false, true, null, 86),
(300, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 86),

/*TEXTILES*/
(301, 'Type', 'Тип', 'Տեսակ', false, true, null, 87),
(302, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 87),

/*RUGS*/
(303,'Type', 'Тип', 'Տեսակ', false, true, null, 88),
(304,'Rug Length', 'Длина Ковра', 'Գորգի Երկարություն', false, true, 3, 88),
(305,'Rug Width', 'Ширина Ковра', 'Գորգի Լայնություն', false, true, 3, 88),
(306,'Condition', 'Состояние', 'Վիճակ', false, true, null, 88),

/*INTERIOR_DECORATION*/
(307,'Type', 'Тип', 'Տեսակ', false, true, null, 89),
(308,'Condition', 'Состояние', 'Վիճակ', false, true, null, 89),

/*TABLEWARE*/
(309,'Type', 'Тип', 'Տեսակ', false, true, null, 90),
(310,'Condition', 'Состояние', 'Վիճակ', false, true, null, 90),

/*COOKING_AND_BAKING*/
(311,'Type', 'Тип', 'Տեսակ', false, true, null, 91),
(312,'Condition', 'Состояние', 'Վիճակ', false, true, null, 91),

/*KITCHEN_ACCESSORIES*/
(313,'Condition', 'Состояние', 'Վիճակ', false, true, null, 92),

/*BATHROOM_ACCESSORIES*/
(314,'Condition', 'Состояние', 'Վիճակ', false, true, null, 93),

/*VIDEO_SURVEILLANCE*/
(315,'Condition', 'Состояние', 'Վիճակ', false, true, null, 94),

/*CAR_PARTS*/
(316,'Type', 'Тип', 'Տեսակ', false, true, null, 95),
(317,'Mark', 'Марка', 'Մակնիշ', false, true, null, 95),
(318,'Originality', 'Оригинальность', 'Օրիգինալ', false, true, null, 95),
(319,'Part Side', 'Сторона', 'Պահեստամասի Կողմ', false, true, null, 95),
(320,'Part Position', 'Положение', 'Պահեստամասի Դիրք', false, true, null, 95),
(321,'Condition', 'Состояние', 'Վիճակ', false, true, null, 95),

/*WHEELS_AND_TIRES*/
(322,'Type', 'Тип', 'Տեսակ', false, true, null, 96),
(323,'Season', 'Сезон', 'Եղանակ', false, true, null, 96),
(324,'Width', 'Ширина', 'Լայնություն', false, true, 4, 96),
(325,'Height', 'Высота', 'Բարձրություն', false, true, 4, 96),
(326,'Diameter', 'Диаметр', 'Տրամագիծ', false, true, 55, 96),
(327,'Condition', 'Состояние', 'Վիճակ', false, true, null, 96),

/*RIMS_AND_HUB_CAPS*/
(328,'Type', 'Тип', 'Տեսակ', false, true, null, 97),
(329,'Diameter', 'Диаметр', 'Տրամագիծ', false, true, 55, 97),
(330,'Condition', 'Состояние', 'Վիճակ', false, true, null, 97),

/*CAR_BATTERIES*/
(331,'Voltage', 'Напряжение', 'Լարում', false, true, 56, 98),
(332,'Capacity', 'Ёмкость', 'Հզորություն', false, true, 28, 98),
(333,'Condition', 'Состояние', 'Վիճակ', false, true, null, 98),

/*GAS_EQUIPMENT*/
(334,'Type', 'Тип', 'Տեսակ', false, true, null, 99),
(335,'Condition', 'Состояние', 'Վիճակ', false, true, null, 99),

/*OILS_AND_CHEMICALS*/
(336,'Type', 'Тип', 'Տեսակ', false, true, null, 100),

/*CAR_ACCESSORIES*/
(337,'Type', 'Тип', 'Տեսակ', false, true, null, 101),

/*CAR_ELECTRONICS*/
(338,'Type', 'Тип', 'Տեսակ', false, true, null, 102),
(339,'Condition', 'Состояние', 'Վիճակ', false, true, null, 102),

/*CAR_AUDIO_AND_VIDEO*/
(340,'Type', 'Тип', 'Տեսակ', false, true, null, 103),
(341,'Condition', 'Состояние', 'Վիճակ', false, true, null, 103),

/*PERSONAL_TRANSPORTATION*/
(342,'Type', 'Тип', 'Տեսակ', false, true, null, 104),
(343,'Condition', 'Состояние', 'Վիճակ', false, true, null, 104),

/*ATVS_AND_SNOWMOBILES*/
(344,'Type', 'Тип', 'Տեսակ', false, true, null, 105),
(345,'Condition', 'Состояние', 'Վիճակ', false, true, null, 105),

/*BOATS_AND_WATER_TRANSPORT*/
(346,'Condition', 'Состояние', 'Վիճակ', false, true, null, 106),

/*TRAILERS_AND_BOOTHS*/
(347,'Type', 'Тип', 'Տեսակ', false, true, null, 107),

(348,'Floor Area', 'Общая площадь', 'Ընդհանուր Մակերես', false, true, 31, 108),
(349,'Exterior Finish', 'Внешняя Отделка', 'Արտաքին Հարդարում', false, true, null, 108),

/*EVENT_VENUES_RENTAL*/
(350,'Type', 'Тип', 'Տեսակ', false, true, null, 109),
(351,'Floor Area', 'Общая площадь', 'Ընդհանուր Մակերես', false, true, null, 109),
(352,'Number of Guests', 'Kоличество Гостей', 'Հյուրերի Քանակ', false, true, null, 109),
(353,'Event Type', 'Типы Мероприятий', 'Միջոցառումների Տեսակներ', false, true, null, 109),
(354,'Facilities', 'Удобства', 'Հարմարություն', false, true, null, 109),
(355,'Equipment', 'Оборудование', 'Սարքավորումներ', false, true, null, 109),

(356,'Noise After Hours', 'Можно Шуметь Вечером', 'Կարելի է Աղմկել Երեկոյան', false, true, null, 110),
(357,'With Pets', 'Можно с Животными', 'Կենդանիների Հետ', false, true, null, 110),

/*WOMEN_CLOTHING_BUY*/
(358,'Type', 'Тип', 'Տեսակ', false, true, null, 111),
(359,'Size', 'Размер', 'Չափս', false, true, null, 111),
(360,'Color', 'Цвет', 'Գույն', false, true, null, 111),
(361,'Condition', 'Состояние', 'Վիճակ', false, true, null, 111),

/*WOMEN_CLOTHING_RENTAL*/
(362,'Type', 'Тип', 'Տեսակ', false, true, null, 112),
(363,'Size', 'Размер', 'Չափս', false, true, null, 112),
(364,'Color', 'Цвет', 'Գույն', false, true, null, 112),
(365,'Condition', 'Состояние', 'Վիճակ', false, true, null, 112),

/*WOMEN_SHOES*/
(366,'Type', 'Тип', 'Տեսակ', false, true, null, 113),
(367,'Season', 'Сезон', 'Եղանակ', false, true, null, 113),
(368,'Shoe Size', 'Размер Обуви', 'Կոշիկի Չափս', false, true, null, 113),
(369,'Color', 'Цвет', 'Գույն', false, true, null, 113),
(370,'Condition', 'Состояние', 'Վիճակ', false, true, null, 113),

/*WOMEN_ACCESSORIES*/
(371,'Type', 'Тип', 'Տեսակ', false, true, null, 114),
(372,'Condition', 'Состояние', 'Վիճակ', false, true, null, 114),

/*MEN_CLOTHING_BUY*/
(373,'Type', 'Тип', 'Տեսակ', false, true, null, 115),
(374,'Size', 'Размер', 'Չափս', false, true, null, 115),
(375,'Color', 'Цвет', 'Գույն', false, true, null, 115),
(376,'Condition', 'Состояние', 'Վիճակ', false, true, null, 115),

/*MEN_SHOES*/
(377,'Type', 'Тип', 'Տեսակ', false, true, null, 116),
(378,'Season', 'Сезон', 'Եղանակ', false, true, null, 116),
(379,'Shoe Size', 'Размер Обуви', 'Կոշիկի Չափս', false, true, null, 116),
(380,'Color', 'Цвет', 'Գույն', false, true, null, 116),
(381,'Condition', 'Состояние', 'Վիճակ', false, true, null, 116),

/*MEN_ACCESSORIES*/
(382,'Type', 'Тип', 'Տեսակ', false, true, null, 117),
(383,'Condition', 'Состояние', 'Վիճակ', false, true, null, 117),

/*JEWELLERY*/
(384,'Type', 'Тип', 'Տեսակ', false, true, null, 118),
(385,'Gender', 'Пол', 'Սեռ', false, true, null, 118),
(386,'Condition', 'Состояние', 'Վիճակ', false, true, null, 118),

/*GLASSES_AND_FRAMES*/
(387,'Type', 'Тип', 'Տեսակ', false, true, null, 119),
(388,'Gender', 'Пол', 'Սեռ', false, true, null, 119),
(389,'Color', 'Цвет', 'Գույն', false, true, null, 119),
(390,'Condition', 'Состояние', 'Վիճակ', false, true, null, 119),

/*WATCHES*/
(391,'Type', 'Тип', 'Տեսակ', false, true, null, 120),
(392,'Gender', 'Пол', 'Սեռ', false, true, null, 120),
(393,'Clock Face', 'Циферблат', 'Թվատախտակ', false, true, null, 120),
(394,'Color', 'Цвет', 'Գույն', false, true, null, 120),
(395,'Condition', 'Состояние', 'Վիճակ', false, true, null, 120),

/*HANDBAGS_AND_WALLETS*/
(396,'Type', 'Тип', 'Տեսակ', false, true, null, 121),
(397,'Gender', 'Пол', 'Սեռ', false, true, null, 121),
(398,'Material', 'Материал', 'Նյութ', false, true, null, 121),
(399,'Color', 'Цвет', 'Գույն', false, true, null, 121),
(400,'Condition', 'Состояние', 'Վիճակ', false, true, null, 121),

/*WORKWEAR_AND_ACCESSORIES*/
(401,'Type', 'Тип', 'Տեսակ', false, true, null, 122),
(402,'Gender', 'Пол', 'Սեռ', false, true, null, 122),
(403,'Size', 'Размер', 'Չափս', false, true, null, 122),
(404,'Color', 'Цвет', 'Գույն', false, true, null, 122),
(405,'Condition', 'Состояние', 'Վիճակ', false, true, null, 122),

/*CARNIVAL_COSTUMES*/
(406,'Type', 'Тип', 'Տեսակ', false, true, null, 123),
(407,'Gender', 'Пол', 'Սեռ', false, true, null, 123),
(408,'Size', 'Размер', 'Չափս', false, true, null, 123),
(409,'Color', 'Цвет', 'Գույն', false, true, null, 123),
(410,'Condition', 'Состояние', 'Վիճակ', false, true, null, 123),

/*WEDDING_DRESSES*/
(411,'Type', 'Тип', 'Տեսակ', false, true, null, 124),
(412,'Size', 'Размер', 'Չափս', false, true, null, 124),
(413,'Condition', 'Состояние', 'Վիճակ', false, true, null, 124),

/*WEDDING_SHOES*/
(414,'Type', 'Тип', 'Տեսակ', false, true, null, 125),
(415,'Shoe Size', 'Размер Обуви', 'Կոշիկի Չափս', false, true, null, 125),
(416,'Condition', 'Состояние', 'Վիճակ', false, true, null, 125),

/*WEDDING_ACCESSORIES*/
(417,'Type', 'Тип', 'Տեսակ', false, true, null, 126),
(418,'Condition', 'Состояние', 'Վիճակ', false, true, null, 126),

/*COLLECTIBLE_ITEMS*/
(419,'Condition', 'Состояние', 'Վիճակ', false, true, null, 127),

/*PAINTINGS_AND_PICTURES*/
(420,'Condition', 'Состояние', 'Վիճակ', false, true, null, 128),

/*ARTS_OBJECTS*/
(421,'Condition', 'Состояние', 'Վիճակ', false, true, null, 129),

/*ARTS_AND_CRAFTS*/
(422,'Condition', 'Состояние', 'Վիճակ', false, true, null, 130),

/*MOTORCYCLES*/
(423,'Type', 'Тип', 'Տեսակ', false, true, null, 131),
(424,'Mark', 'Марка', 'Մակնիշ', false, true, null, 131),
(425,'Year', 'Год', 'Տարեթիվ', false, true, null, 131),
(426,'Engine Type', 'Тип Двигателя', 'Շարժիչի Տեսակ', false, true, null, 131),
(427,'Engine Size', 'Объём Двигателя', 'Շարժիչի Ծավալ', false, true, null, 131),
(428,'Transmission', 'Коробка Передач', 'Փոխանցման Տուփ', false, true, null, 131),

(429,'Mileage', 'Пробег', 'Վազք', false, true, 2, 132),
(430,'Color', 'Цвет', 'Գույն', false, true, null, 132),

/*MOTORCYCLE_PARTS_AND_ACCESSORIES*/
(431,'Condition', 'Состояние', 'Վիճակ', false, true, null, 133),

/*GUITARS*/
(432,'Type', 'Тип', 'Տեսակ', false, true, null, 134),
(433,'Condition', 'Состояние', 'Վիճակ', false, true, null, 134),

/*PIANOS_AND_KEYBOARD_INSTRUMENTS*/
(434,'Type', 'Тип', 'Տեսակ', false, true, null, 135),
(435,'Condition', 'Состояние', 'Վիճակ', false, true, null, 135),

/*BRASS_AND_WOODWIND_INSTRUMENTS*/
(436,'Type', 'Тип', 'Տեսակ', false, true, null, 136),
(437,'Condition', 'Состояние', 'Վիճակ', false, true, null, 136),

/*STRING_INSTRUMENTS*/
(438,'Type', 'Тип', 'Տեսակ', false, true, null, 137),
(439,'Condition', 'Состояние', 'Վիճակ', false, true, null, 137),

/*ACCORDIONS*/
(440,'Type', 'Тип', 'Տեսակ', false, true, null, 138),
(441,'Condition', 'Состояние', 'Վիճակ', false, true, null, 138),

/*DRUMS_AND_PERCUSSION_INSTRUMENTS*/
(442,'Type', 'Тип', 'Տեսակ', false, true, null, 139),
(443,'Condition', 'Состояние', 'Վիճակ', false, true, null, 139),

/*STUDIO_ACCESSORIES*/
(444,'Type', 'Тип', 'Տեսակ', false, true, null, 140),
(445,'Condition', 'Состояние', 'Վիճակ', false, true, null, 140),

/*HUNTING_AND_FISHING*/
(446,'Condition', 'Состояние', 'Վիճակ', false, true, null, 141),

/*CAMPING_EQUIPMENT*/
(447,'Condition', 'Состояние', 'Վիճակ', false, true, null, 142),

/*FITNESS_AND_EXERCISE_EQUIPMENT*/
(448,'Condition', 'Состояние', 'Վիճակ', false, true, null, 143),

/*BILLIARD_AND_BOWLING*/
(449,'Condition', 'Состояние', 'Վիճակ', false, true, null, 144),

/*FOOTBALL_AND_BALL_GAMES*/
(450,'Condition', 'Состояние', 'Վիճակ', false, true, null, 145),

/*WATER_SPORTS*/
(451,'Condition', 'Состояние', 'Վիճակ', false, true, null, 146),

/*WINTER_SPORTS_EQUIPMENT*/
(452,'Condition', 'Состояние', 'Վիճակ', false, true, null, 147),

/*BOXING_AND_MARTIAL_ARTS*/
(453,'Condition', 'Состояние', 'Վիճակ', false, true, null, 148),

/*TENNIS_AND_BADMINTON*/
(454,'Condition', 'Состояние', 'Վիճակ', false, true, null, 149),

/*MOUNTAINEERING*/
(455,'Condition', 'Состояние', 'Վիճակ', false, true, null, 150),

/*BOOKS_AND_MAGAZINES*/
(456,'Condition', 'Состояние', 'Վիճակ', false, true, null, 151),

/*FILMS_AND_MUSIC*/
(457,'Condition', 'Состояние', 'Վիճակ', false, true, null, 152),

/*DOGS*/
(458,'Gender', 'Пол', 'Սեռ', false, true, null, 153),
(459, 'Age', 'Возраст', 'Տարիք', false, true, null, 153),

/*CATS*/
(460,'Gender', 'Пол', 'Սեռ', false, true, null, 154),
(461, 'Age', 'Возраст', 'Տարիք', false, true, null, 154),

/*FISH*/
(462,'Gender', 'Пол', 'Սեռ', false, true, null, 155),
(463, 'Nutrition Type', 'Тип Питания', 'Կերատեսակ', false, true, null, 155),

/*BIRDS*/
(464, 'Nutrition Type', 'Тип Питания', 'Կերատեսակ', false, true, null, 156),

/*RODENTS*/
(465, 'Gender', 'Пол', 'Սեռ', false, true, null, 157),

/*REPTILES*/
(466, 'Type', 'Тип', 'Տեսակ', false, true, null, 158),

/*CATTLE*/
(467,'Gender', 'Пол', 'Սեռ', false, true, null, 159),
(468, 'Age', 'Возраст', 'Տարիք', false, true, null, 159),

/*HORSES*/
(469,'Gender', 'Пол', 'Սեռ', false, true, null, 160),
(470, 'Age', 'Возраст', 'Տարիք', false, true, null, 160),

/*PIGS_AND_PIGLETS*/
(471,'Gender', 'Пол', 'Սեռ', false, true, null, 161),
(472, 'Age', 'Возраст', 'Տարիք', false, true, null, 161),

/*SHEEP_AND_GOAT*/
(473,'Gender', 'Пол', 'Սեռ', false, true, null, 162),
(474, 'Age', 'Возраст', 'Տարիք', false, true, null, 162),

/*RABBITS*/
(475,'Gender', 'Пол', 'Սեռ', false, true, null, 163),
(476, 'Age', 'Возраст', 'Տարիք', false, true, null, 163),

/*GIRLS_CLOTHING*/
(477, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 164),

/*BOYS_CLOTHING*/
(478, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 165),

/*BABIES_CLOTHING*/
(479, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 166),

/*SHOES_FOR_GIRLS*/
(480, 'Season', 'Сезон', 'Եղանակ', false, true, null, 167),
(481, 'Shoe Size', 'Размер Обуви', 'Կոշիկի Չափս', false, true, null, 167),
(482, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 167),

/*SHOES_FOR_BOYS*/
(483, 'Season', 'Сезон', 'Եղանակ', false, true, null, 168),
(484, 'Shoe Size', 'Размер Обуви', 'Կոշիկի Չափս', false, true, null, 168),
(485, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 168),

/*KIDS_TRANSPORTATION*/
(486, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 169),

/*BUILDING_SETS*/
(487, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 170),

/*LEARNING_AND_EDUCATIONAL_TOYS*/
(488, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 171),

/*TOYS_FOR_GIRLS*/
(489, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 172),

/*TOYS_FOR_BOYS*/
(490, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 173),

/*TOYS_FOR_NEWBORNS*/
(491, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 174),

/*OUTDOORS_AND_SEASONAL_TOYS*/
(492, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 175),

/*PRODUCTS_FOR_BABIES*/
(493, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 176),

/*STROLLERS*/
(494, 'Type', 'Тип', 'Տեսակ', false, true, null, 177),
(495, 'For Ages', 'Для Возраста', 'Տարիք', false, true, null, 177),
(496, 'Number of Seats', 'Количестви Мест', 'Տեղերի Քանակ', false, true, null, 177),
(497, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 177),

/*PRODUCTS_FOR_BABIES*/
(498, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 178),

/*BABY_CARRIERS*/
(499, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 179),

/*WALKERS*/
(500, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 180),

/*SWINGS*/
(501, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 181),

/*PLAYPENS*/
(502, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 182),

/*BED_ACCESSORIES_AND_DECOR*/
(503, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 183),

/*KIDS_FURNITURE*/
(504, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 184),

/*HIGH_CHAIRS*/
(505, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 185),

/*FEEDING*/
(506, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 186),

/*BATH_AND_HYGIENE*/
(507, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 187),

/*BACKPACKS_AND_BAGS*/
(508, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 188),

/*SCHOOL_SUPPLIES*/
(509, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 189),

/*PRODUCTS_FOR_DOGS*/
(510, 'Type', 'Тип', 'Տեսակ', false, true, null, 190),

/*PRODUCTS_FOR_CATS*/
(511, 'Type', 'Тип', 'Տեսակ', false, true, null, 191),

/*PRODUCTS_FOR_FISH_AND_REPTILES*/
(512, 'Type', 'Тип', 'Տեսակ', false, true, null, 192),

/*PRODUCTS_FOR_RODENTS*/
(513, 'Type', 'Тип', 'Տեսակ', false, true, null, 193),

/*PRODUCTS_FOR_FARM_ANIMALS*/
(514, 'Type', 'Тип', 'Տեսակ', false, true, null, 194),

/*PRODUCTS_FOR_BIRDS*/
(515, 'Type', 'Тип', 'Տեսակ', false, true, null, 195),

/*HAND_TOOLS*/
(516, 'Type', 'Тип', 'Տեսակ', false, true, null, 196),
(517, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 196),

/*MACHINE_TOOLS*/
(518, 'Type', 'Тип', 'Տեսակ', false, true, null, 197),
(519, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 197),

/*ELECTRICAL_DEVICES*/
(520, 'Type', 'Тип', 'Տեսակ', false, true, null, 198),
(521, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 198),

/*MEASURING_EQUIPMENT*/
(522, 'Type', 'Тип', 'Տեսակ', false, true, null, 199),
(523, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 199),

/*SAWS*/
(524, 'Type', 'Тип', 'Տեսակ', false, true, null, 200),
(525, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 200),

/*WELDING_EQUIPMENT*/
(526, 'Type', 'Тип', 'Տեսակ', false, true, null, 201),
(527, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 201),

/*CONCRETE_МIXERS*/
(528, 'Type', 'Тип', 'Տեսակ', false, true, null, 202),
(529, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 202),

/*LADDERS_AND_STEPLADDERS*/
(530, 'Type', 'Тип', 'Տեսակ', false, true, null, 203),
(531, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 203),

/*SCAFFOLDING_AND_TOWERS*/
(532, 'Type', 'Тип', 'Տեսակ', false, true, null, 204),
(533, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 204),

/*PROTECTIVE_EQUIPMENT*/
(534, 'Type', 'Тип', 'Տեսակ', false, true, null, 205),
(535, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 205),

/*GARDENING_EQUIPMENT*/
(536, 'Type', 'Тип', 'Տեսակ', false, true, null, 206),
(537, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 206),

/*FAUCETS*/
(538, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 207),

/*SHOWERS_AND_BATHS*/
(539, 'Type', 'Тип', 'Տեսակ', false, true, null, 208),
(540, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 208),

/*SINKS_AND_WASHBASINS*/
(541, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 209),

/*PUMPS*/
(542, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 210),

/*WATER_METERS*/
(543, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 211),

/*TOILETS_AND_BIDETS*/
(544, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 212),

/*WINDOWS*/
(545, 'Type', 'Тип', 'Տեսակ', false, true, null, 213),
(546, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 213),

/*DOORS*/
(547, 'Type', 'Тип', 'Տեսակ', false, true, null, 214),
(548, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 214),

/*GATES_AND_FENCES*/
(549, 'Type', 'Тип', 'Տեսակ', false, true, null, 215),
(550, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 215),

/*FLOORING*/
(551, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 216),

/*WATER_SUPPLY_AND_PIPES*/
(552, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 217),

/*RETAIL_AND_SHOPS_EQUIPMENT*/
(553, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 218),

/*OFFICE_EQUIPMENT*/
(554, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 219),

/*MANUFACTURING_EQUIPMENT*/
(555, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 220),

/*RESTAURANTS_AND_CAFES_EQUIPMENT*/
(556, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 221),

/*BEAUTY_SALONS_EQUIPMENT*/
(557, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 222),

/*CAR_SERVICES_EQUIPMENT*/
(558, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 223),

/*AGRICULTURAL_EQUIPMENT*/
(559, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 224),

/*WAREHOUSE_EQUIPMENT*/
(560, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 225),

/*ATTRACTIONS_AND_VENDING_MACHINES*/
(561, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 226),

/*ADVERTISING_AND_EXHIBITION_EQUIPMENT*/
(562, 'Condition', 'Состояние', 'Վիճակ', false, true, null, 227),

/*BUSINESSES_SALE*/
(563, 'Type', 'Тип', 'Տեսակ', false, true, null, 228),

/*BUSINESSES_RENTAL*/
(564, 'Type', 'Тип', 'Տեսակ', false, true, null, 229)
ON CONFLICT (id) DO NOTHING;