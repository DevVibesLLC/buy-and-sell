INSERT INTO field_name_entity (id, field_name, is_prefilled, is_required, measurement_id, description_id)
VALUES

/*CAR*/
(1, 'Mark', false, true, null, 1),
(2, 'Model', false, true, null, 1),
(3, 'Year', false, true, null, 1),
(4, 'Body Type', false, true, null, 1),
(5, 'Engine',false,true,null,1),
(6, 'Engine Size', false, true, 40, 1),
(7, 'Transmission', false, true, null, 1),
(8, 'Drive Type', false, true, null, 1),

(9, 'Mileage', false, true, 2, 2),
(10, 'Steering Wheel', false, true, null, 2),
(11, 'Cleared Customs', false, true, null, 2),

(12, 'Color', false, true, null, 3),
(13, 'Wheel Size', false, true, 55, 3),
(14, 'Headlights', false, true, null, 3),

(15, 'Interior Color', false, true, null, 4),
(16, 'Interior Material', false, true, null, 4),
(17, 'Sunroof', false, true, null, 4),

/*TRUCK*/
(18, 'Mark', false, true, null, 5),
(19, 'Model', false, true, null, 5),
(20, 'Year', false, true, null, 5),
(21, 'Engine Type', false, true, null, 5),
(22, 'Transmission', false, true, null, 5),
(23, 'Chassis configuration',false, true, null,5),

(24, 'Mileage', false, true, 2, 6),
(25, 'Steering Wheel', false, true, null, 6),
(26, 'Cleared Customs', false, true, null, 6),
(27, 'Color', false, true, null, 6),

/*BUS*/
(28, 'Mark', false, true, null, 7),
(29, 'Model', false, true, null, 7),
(30, 'Year', false, true, null, 7),
(31, 'Engine Type', false, true, null, 7),
(32, 'Transmission', false, true, null, 7),

(33, 'Mileage', false, true, 2, 8),
(34, 'Steering Wheel', false, true, null, 8),
(35, 'Cleared Customs', false, true, null, 8),
(36, 'Color', false, true, null, 8),

/*APARTMENT BUY*/
(37, 'Construction Type', false, true, null, 9),
(38, 'New Construction', false, true, null, 9),
(39, 'Elevator', false, true, null, 9),
(40, 'Floors in the Building', false, true, null, 9),
(41, 'The House Has', false, true, null, 9),
(42, 'Parking', false, true, null, 9),

(43, 'Floor Area', false, true, 31, 10),
(44, 'Number of Rooms', false, true, null, 10),
(45, 'Number of Bathrooms', false, true, null, 10),
(46, 'Ceiling Height', false, true, 1, 10),
(47, 'Floor', false, true, null, 10),
(48, 'Balcony', false, true, null, 10),
(49, 'Furniture', false, true, null, 10),
(50, 'Renovation', false, true, null, 10),
(51, 'Appliances', false, true, null, 10),
(52, 'Window Views', false, true, null, 10),

/*APARTMENT RENTAL*/
(53, 'Construction Type', false, true, null, 11),
(54, 'New Construction', false, true, null, 11),
(55, 'Elevator', false, true, null, 11),
(56, 'Floors in the Building', false, true, null, 11),
(57, 'The House Has', false, true, null, 11),
(58, 'Parking', false, true, null, 11),

(59, 'Floor Area', false, true, 31, 12),
(60, 'Number of Rooms', false, true, null, 12),
(61, 'Number of Bathrooms', false, true, null, 12),
(62, 'Ceiling Height', false, true, 1, 12),
(63, 'Floor', false, true, null, 12),
(64, 'Balcony', false, true, null, 12),
(65, 'Furniture', false, true, null, 12),
(66, 'Renovation', false, true, null, 12),
(67, 'Amenities', false, true, null, 12),
(68, 'Appliances', false, true, null, 12),
(69, 'Window Views', false, true, null, 12),

(70, 'With Children', false, true, null, 13),
(71, 'With Pets', false, true, null, 13),

(72, 'Utility Payments', false, true, null, 14),
(73, 'Prepayment', false, true, null, 14),

/*HOUSE BUY*/
(74, 'Type',false,true,null,15),
(75, 'Condition',false,true,null,15),
(76, 'Construction Type',false,true,null,15),
(77, 'House Area',false,true,31,15),
(78, 'Floors in the Building',false,true,null,15),
(79, 'Number of Rooms',false,true,null,15),
(80, 'Number of Bathrooms',false,true,null,15),
(81, 'Furniture',false,true,null,15),
(82, 'Garage',false,true,null,15),
(83, 'Renovation',false,true,null,15),
(84, 'Facilities',false,true,null,15),
(85, 'Appliances',false,true,null,15),
(86, 'Service Lines',false,true,null,15),

(87, 'Land Area',false,true,31,16),

(88, 'Type',false,true,null,17),
(89, 'Construction Type',false,true,null,17),
(90, 'House Area',false,true,31,17),
(91, 'Floors in the Building',false,true,null,17),
(92, 'Number of Rooms',false,true,null,17),
(93, 'Number of Bathrooms',false,true,null,17),
(94, 'Furniture',false,true,null,17),
(95, 'Garage',false,true,null,17),
(96, 'Renovation',false,true,null,17),
(97, 'Amenities',false,true,null,17),
(98, 'Appliances',false,true,null,17),
(99, 'Service Lines',false,true,null,17),

(100, 'Land Area', false, true, 31, 18),

(101, 'With Children', false, true, null, 19),
(102, 'With Pets', false, true, null, 19),

(103, 'Utility Payments', false, true, null, 20),
(104, 'Prepayment', false, true, null, 20),

(105, 'Type',false,true,null,21),
(106, 'Construction Type',false,true,null,21),
(107, 'Floor Area',false,true,31,21),
(108, 'Furniture',false,true,null,21),
(109, 'Elevator',false,true,null,21),
(110, 'Location from the Street',false,true,null,21),
(111, 'Entrance',false,true,null,21),
(112, 'Parking',false,true,null,21),

(113, 'Type',false,true,null,22),
(114, 'Floor Area',false,true,31,22),
(115, 'Furniture',false,true,null,22),
(116, 'Elevator',false,true,null,22),
(117, 'Location from the Street',false,true,null,22),
(118, 'Entrance',false,true,null,22),
(119, 'Parking',false,true,null,22),

(120, 'Lease Type', false, true, null,23),
(121, 'Minimum Rental Period', false, true, null,23),
(122, 'Utility Payments', false, true, null, 23),
(123, 'Prepayment', false, true, null, 23),

(124, 'Type',false,true,null,24),
(125, 'Floor Area',false,true,31,24),
(126, 'Utilities',false,true,null,24),
(127, 'Amenities',false,true,null,24),

(128, 'Type',false,true,null,25),
(129, 'Floor Area',false,true,31,25),
(130, 'Utilities',false,true,null,25),
(131, 'Amenities',false,true,null,25),

(132, 'Prepayment', false, true, null, 26),

(133, 'Type', false,true,null, 27),
(134, 'Land Area', false,true,null, 27),
(135, 'Service Lines', false,true,null, 27),

(136, 'Type', false,true,null, 28),
(137, 'Land Area', false,true,null, 28),
(138, 'Service Lines', false,true,null, 28),

(139, 'Prepayment', false, true, null, 29),

(140, 'Construction Type', false, true, null, 30),
(141, 'Elevator', false, true, null, 30),
(142, 'Floors in the Building', false, true, null, 30),
(143, 'The House Has', false, true, null, 30),
(144, 'Parking', false, true, null, 30),

(145, 'Floor Area', false, true, 31, 31),
(146, 'Number of Rooms', false, true, null, 31),
(147, 'Number of Bathrooms', false, true, null, 31),
(148, 'Ceiling Height', false, true, 1, 31),
(149, 'Floor', false, true, null, 31),
(150, 'Balcony', false, true, null, 31),

(151, 'Interior Finishing', false, true, null, 32),
(152, 'Handover Date', false, true, null, 32),

(153, 'Mortgage is Possible', false, true, null, 33),

(154, 'Type', false, true, null, 34),
(155, 'Construction Type', false, true, null, 34),
(156, 'House Area', false, true, null, 34),
(157, 'Floors in the Building', false, true, null, 34),
(158, 'Number of Rooms', false, true, null, 34),
(159, 'Number of Bathrooms', false, true, null, 34),
(160, 'Garage', false, true, null, 34),
(161, 'Service Lines', false, true, null, 34),

(162, 'Land Area', false, true, null, 35),

(163, 'Interior Finishing', false, true, null, 36),
(164, 'Handover Date', false, true, null, 36),

(165, 'Mortgage is Possible', false, true, null, 37),

(166, 'Construction Type', false, true, null, 38),
(167, 'New Construction', false, true, null, 38),
(168, 'Elevator', false, true, null, 38),
(169, 'Floors in the Building', false, true, null, 38),
(170, 'The House Has', false, true, null, 38),
(171, 'Parking', false, true, null, 38),

(172, 'Floor Area', false, true, 31, 39),
(173, 'Number of Rooms', false, true, null, 39),
(174, 'Number of Bathrooms', false, true, null, 39),
(175, 'Ceiling Height', false, true, 1, 39),
(176, 'Floor', false, true, null, 39),
(177, 'Balcony', false, true, null, 39),
(178, 'Renovation', false, true, null, 39),
(179, 'Comfort', false, true, null, 39),
(180, 'Amenities', false, true, null, 39),
(181, 'Appliances', false, true, null, 39),
(182, 'Window Views', false, true, null, 39),

(183, 'Number of Guests', false, true, null, 40),
(184, 'With Children', false, true, null, 40),
(185, 'With Pets', false, true, null, 40),

(186, 'Type', false, true, null, 41),
(187, 'Construction Type', false, true, null, 41),
(188, 'House Area', false, true, null, 41),
(189, 'Floors in the Building', false, true, null, 41),
(190, 'Number of Rooms', false, true, null, 41),
(191, 'Number of Bathrooms', false, true, null, 41),
(192, 'Garage', false, true, null, 41),
(193, 'Renovation', false, true, null, 41),
(194, 'Comfort', false, true, null, 41),
(195, 'Amenities', false, true, null, 41),
(196, 'Appliances', false, true, null, 41),

(197, 'Land Area', false, true, null, 42),

(198, 'Number of Guests', false, true, null, 43),
(199, 'With Children', false, true, null, 43),
(200, 'With Pets', false, true, null, 43)
ON CONFLICT (id) DO NOTHING;
