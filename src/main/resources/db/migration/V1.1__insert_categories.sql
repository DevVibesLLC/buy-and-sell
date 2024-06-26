insert into category_entity (id, name)
values
(1, 'CAR'),
(2, 'TRUCK'),
(3, 'BUS'),
(4, 'APARTMENT_BUY'),
(5, 'APARTMENT_RENTAL'),
(6, 'HOUSE_BUY'),
(7, 'HOUSE_RENTAL'),
(8, 'COMMERCIAL_BUY'),
(9, 'COMMERCIAL_RENTAL'),
(10, 'GARAGE_AND_PARKING_BUY'),
(11, 'GARAGE_AND_PARKING_RENTAL'),
(12, 'LAND_BUY'),
(13, 'LAND_RENTAL'),
(14, 'NEW_CONSTRUCTION_APARTMENT'),
(15, 'NEW_CONSTRUCTION_HOUSE'),
(16, 'APARTMENT_DAILY_RENTAL'),
(17, 'HOUSE_DAILY_RENTAL'),
(18, 'MOBILE_PHONE'),
(19, 'NOTEBOOK'),
(20, 'COMPUTER'),
(21, 'SMART_WATCH'),
(22, 'TABLET'),
(23, 'TV'),
(24, 'GAMING_CONSOLE'),
(25, 'HEADPHONE'),
(26, 'COMPUTER_AND_NOTEBOOK_PARTS'),
(27, 'PHOTO_AND_VIDEO_CAMERA'),
(28, 'COMPUTER_GAMES'),
(29, 'SMART_HOME_ACCESSORIES'),
(30, 'WASHER'),
(31, 'CLOTHES_DRYER'),
(32, 'IRON_AND_ACCESSORIES'),
(33, 'REFRIGERATOR'),
(34, 'FREEZER'),
(35, 'DISHWASHER'),
(36, 'MICROWAVE'),
(37, 'STOVE'),
(38, 'COFFEE_MAKER_AND_ACCESSORIES')
ON CONFLICT (id) DO NOTHING;