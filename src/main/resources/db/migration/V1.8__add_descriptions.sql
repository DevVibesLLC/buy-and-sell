INSERT INTO description_entity (id, header, category_id)
VALUES
/*CAR*/
(1, 'SPECIFICATIONS', 1),
(2, 'ADDITIONAL_INFORMATION', 1),
(3, 'EXTERIOR', 1),
(4, 'INTERIOR', 1),

/*TRUCK*/
(5, 'SPECIFICATIONS',2),
(6, 'ADDITIONAL_INFORMATION', 2),

/*BUS*/
(7, 'SPECIFICATIONS',3),
(8, 'ADDITIONAL_INFORMATION', 3),

/*APARTMENT BUY*/
(9, 'BUILDING_INFORMATION',4),
(10, 'APARTMENT_INFORMATION', 4),

/*APARTMENT RENTAL*/
(11, 'BUILDING_INFORMATION',5),
(12, 'APARTMENT_INFORMATION', 5),
(13, 'HOUSE_RULES', 5),
(14, 'DEAL_TERMS', 5),

/*HOUSE BUY*/
(15, 'HOUSE_INFORMATION',6),
(16, 'LOT_INFORMATION', 6),

/*HOUSE RENTAL*/
(17, 'HOUSE_INFORMATION',7),
(18, 'LOT_INFORMATION', 7),
(19, 'HOUSE_RULES',7),
(20, 'DEAL_TERMS',7),

/*COMMERCIAL BUY*/
(21, 'ADDITIONAL_INFORMATION', 8),

/*COMMERCIAL RENTAL*/
(22, 'ADDITIONAL_INFORMATION', 9),
(23, 'DEAL_TERMS',9),

/*GARAGE AND PARKING BUY*/
(24, 'ADDITIONAL_INFORMATION', 10),

/*GARAGE AND PARKING RENTAL*/
(25, 'ADDITIONAL_INFORMATION', 11),
(26, 'DEAL_TERMS', 11),

/*LAND BUY*/
(27, 'ADDITIONAL_INFORMATION', 12),

/*LAND RENTAL*/
(28, 'ADDITIONAL_INFORMATION', 13),
(29, 'DEAL_TERMS', 13),

/*NEW CONSTRUCTION APARTMENT*/
(30, 'BUILDING_INFORMATION',14),
(31, 'APARTMENT_INFORMATION', 14),
(32, 'STAGE_OF_PREPARATION', 14),
(33, 'DEAL_TERMS', 14),

/*NEW CONSTRUCTION HOUSE*/
(34, 'HOUSE_INFORMATION',15),
(35, 'LOT_INFORMATION', 15),
(36, 'STAGE_OF_PREPARATION', 15),
(37, 'DEAL_TERMS', 15),

/*APARTMENT DAILY RENTAL*/
(38, 'BUILDING_INFORMATION',16),
(39, 'APARTMENT_INFORMATION', 16),
(40, 'HOUSE_RULES',16),

/*HOUSE DAILY RENTAL*/
(41, 'HOUSE_INFORMATION',17),
(42, 'LOT_INFORMATION', 17),
(43, 'HOUSE_RULES',17),

/*MOBILE PHONE*/
(44,'SPECIFICATIONS',18),

/*NOTEBOOK*/
(45, 'SPECIFICATIONS', 19),

/*COMPUTER*/
(46, 'SPECIFICATIONS', 20),

/*SMART WATCH*/
(47, 'SPECIFICATIONS', 21),

/*TABLET*/
(48, 'SPECIFICATIONS', 22),

/*TV*/
(49, 'SPECIFICATIONS', 23),

/*GAMING CONSOLE*/
(50, 'SPECIFICATIONS', 24),

/*HEADPHONE*/
(51, 'SPECIFICATIONS', 25),

/*COMPUTER AND NOTEBOOK PARTS*/
(52, 'SPECIFICATIONS', 26),

/*PHOTO AND VIDEO CAMERA*/
(53, 'SPECIFICATIONS', 27),

/*COMPUTER GAMES*/
(54, 'SPECIFICATIONS', 28),

/*SMART HOME ACCESSORIES*/
(55, 'SPECIFICATIONS', 29),

/*WASHER*/
(56, 'SPECIFICATIONS', 30),

/*CLOTHES DRYER*/
(57, 'SPECIFICATIONS', 31),

/*IRON AND ACCESSORIES*/
(58, 'SPECIFICATIONS', 32)

ON CONFLICT (id) DO NOTHING;