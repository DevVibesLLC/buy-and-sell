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
(18, 'MOBILE_PHONE')
ON CONFLICT (id) DO NOTHING;