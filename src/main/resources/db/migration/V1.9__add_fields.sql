INSERT INTO field_name_entity (id, field_name, is_prefilled, is_required, measurement_id, description_id)
VALUES

/*CAR*/
(1, 'Mark', false, true, null, 1),
(2, 'Model', false, true, null, 1),
(3, 'Year', false, true, null, 1),
(4, 'Body Type', false, true, null, 1),
(5, 'Engine Type',false,true,null,1),
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

/*HOUSE RENTAL*/
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

/*COMMERCIAL BUY*/
(105, 'Type',false,true,null,21),
(106, 'Construction Type',false,true,null,21),
(107, 'Floor Area',false,true,31,21),
(108, 'Furniture',false,true,null,21),
(109, 'Elevator',false,true,null,21),
(110, 'Location from the Street',false,true,null,21),
(111, 'Entrance',false,true,null,21),
(112, 'Parking',false,true,null,21),

/*COMMERCIAL RENTAL*/
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

/*GARAGE AND PARKING BUY*/
(124, 'Type',false,true,null,24),
(125, 'Floor Area',false,true,31,24),
(126, 'Utilities',false,true,null,24),
(127, 'Amenities',false,true,null,24),

/*GARAGE AND PARKING RENTAL*/
(128, 'Type',false,true,null,25),
(129, 'Floor Area',false,true,31,25),
(130, 'Utilities',false,true,null,25),
(131, 'Amenities',false,true,null,25),

(132, 'Prepayment', false, true, null, 26),

/*LAND BUY*/
(133, 'Type', false,true,null, 27),
(134, 'Land Area', false,true,null, 27),
(135, 'Service Lines', false,true,null, 27),

/*LAND RENTAL*/
(136, 'Type', false,true,null, 28),
(137, 'Land Area', false,true,null, 28),
(138, 'Service Lines', false,true,null, 28),

(139, 'Prepayment', false, true, null, 29),

/*NEW CONSTRUCTION APARTMENT*/
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

/*NEW CONSTRUCTION HOUSE*/
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

/*APARTMENT DAILY RENTAL*/
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

/*HOUSE DAILY RENTAL*/
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
(200, 'With Pets', false, true, null, 43),

/*MOBILE PHONE*/
(201, 'Mark', false, true, null, 44),
(202, 'Model', false, true, null, 44),
(203, 'Condition', false, true, null, 44),
(204, 'Memory (RAM)', false, true, null, 44),
(205, 'Memory', false, true, null, 44),
(206, 'Color', false, true, null, 44),

/*NOTEBOOK*/
(207, 'Mark',false,true,null,45),
(208, 'Condition',false,true,null,45),
(209, 'Processor', false, true, null, 45),
(210, 'Memory (RAM)', false, true, null, 45),
(211, 'Memory', false, true, null, 45),
(212, 'Screen Resolution',false,true,null,45),
(213, 'Screen Size',false,true,10,45),

/*COMPUTER*/
(214, 'Condition', false, true, null, 46),
(215, 'Processor', false, true, null, 46),
(216, 'Memory (RAM)', false, true, null, 46),
(217, 'Memory', false, true, null, 46),
(218, 'Screen Resolution', false, true, null, 46),
(219, 'Screen Size',false,true,10,46),

/*SMART WATCH*/
(220, 'Mark', false, true, null, 47),
(221, 'Condition', false, true, null, 47),
(222, 'Color', false, true, null, 47),

/*TABLET*/
(223, 'Mark', false, true, null, 48),
(224, 'Condition', false, true, null, 48),
(225, 'Screen Size',false,true,10,48),
(226, 'Memory', false, true, null, 48),
(227, 'Color', false, true, null, 48),

/*TV*/
(228, 'Mark', false, true, null, 49),
(229, 'Condition', false, true, null, 49),
(230, 'Screen Size',false,true, 10, 49),

/*GAMING CONSOLE*/
(231, 'Mark', false, true, null, 50),
(232, 'Type', false, true, null, 50),
(233, 'Condition', false, true, null, 50),

/*HEADPHONE*/
(234, 'Mark', false, true, null, 51),
(235, 'Connection Type', false, true, null, 51),
(236, 'Condition', false, true, null, 51),
(237, 'Color', false, true, null, 51),

/*COMPUTER AND NOTEBOOK PARTS*/
(238, 'Type', false, true, null, 52),
(239, 'Condition', false, true, null, 52),

/*PHOTO AND VIDEO CAMERA*/
(240, 'Mark', false, true, null, 53),
(241, 'Condition', false, true, null, 53),

/*COMPUTER GAMES*/
(242, 'Type', false, true, null, 54),
(243, 'Condition', false, true, null, 54),

/*SMART HOME ACCESSORIES*/
(244, 'Mark', false, true, null, 55),
(245, 'Type', false, true, null, 55),
(246, 'Condition', false, true, null, 55),

/*WASHER*/
(247, 'Mark', false, true, null, 56),
(248, 'Type', false, true, null, 56),
(249, 'Maximum Laundry Capacity', false, true, 11, 56),
(250, 'Laundry Load Type', false, true, null, 56),
(251, 'Condition', false, true, null, 56),

/*CLOTHES DRYER*/
(252, 'Condition',false, true, null, 57),

/*IRON AND ACCESSORIES*/
(253, 'Type', false, true, null, 58),
(254, 'Condition', false, true, null, 58),

/*REFRIGERATOR*/
(255, 'Mark', false, true, null, 59),
(256, 'Type', false, true, null, 59),
(257, 'Condition', false, true, null, 59),

/*FREEZER*/
(258, 'Condition', false, true, null, 60),

/*DISHWASHER*/
(259, 'Condition', false, true, null, 61),

/*MICROWAVE*/
(260, 'Condition', false, true, null, 62),

/*STOVE*/
(261, 'Type', false, true, null, 63),
(262, 'Burner Type', false, true, null, 63),
(263, 'Condition', false, true, null, 63),

/*COFFEE MAKER AND ACCESSORIES*/
(264, 'Type', false, true, null, 64),
(265, 'Condition', false, true, null, 64),

/*KETTLES*/
(266, 'Type', false, true, null, 65),
(267, 'Condition', false, true, null, 65),

/*RANGE_HOODS*/
(268, 'Condition', false, true, null, 66),

/*VACUUM_CLEANERS*/
(269, 'Condition', false, true, null, 67),

/*ROBOTIC_VACUUMS*/
(270, 'Condition', false, true, null, 68),

/*FLOOR_WASHERS*/
(271, 'Condition', false, true, null, 69),

/*AIR_CONDITIONERS*/
(272, 'Condition', false, true, null, 70),

/*WATER_HEATERS*/
(273, 'Condition', false, true, null, 71),

/*AIR_PURIFIERS_AND_HUMIDIFIERS*/
(274, 'Type', false, true, null, 72),
(275, 'Condition', false, true, null, 72),

/*COMPUTERS_PERIPHERALS*/
(276, 'Type', false, true, null, 73),
(277, 'Condition', false, true, null, 73),

/*AUDIO_PLAYERS_AND_STEREOS*/
(278, 'Type', false, true, null, 74),

(279, 'Condition', false, true, null, 75),

/*QUADCOPTERS_AND_DRONES*/
(280, 'Condition', false, true, null, 76),

/*SOFAS_AND_ARMCHAIRS*/
(281, 'Type', false, true, null, 77),
(282, 'Upholstery', false, true, null, 77),
(283, 'Color', false, true, null, 77),
(284, 'Condition', false, true, null, 77),

/*STORAGE*/
(285, 'Type', false, true, null, 78),
(286, 'Condition', false, true, null, 78),

/*TABLES_AND_CHAIRS*/
(287, 'Type', false, true, null, 79),
(288, 'Condition', false, true, null, 79),

/*BEDROOM_FURNITURE*/
(289, 'Type', false, true, null, 80),
(290, 'Condition', false, true, null, 80),

/*KITCHEN_FURNITURE*/
(291, 'Type', false, true, null, 81),
(292, 'Color', false, true, null, 81),
(293, 'Condition', false, true, null, 81),

/*GARDEN_FURNITURE*/
(294, 'Type', false, true, null, 82),
(295, 'Condition', false, true, null, 82),

/*BARBECUE_AND_ACCESSORIES*/
(296, 'Condition', false, true, null, 83),

/*GARDEN_DECOR*/
(297, 'Condition', false, true, null, 84),

/*GARDEN_ACCESSORIES*/
(298, 'Condition', false, true, null, 85),

/*LIGHTING*/
(299, 'Type', false, true, null, 86),
(300, 'Condition', false, true, null, 86),

/*TEXTILES*/
(301, 'Type', false, true, null, 87),
(302, 'Condition', false, true, null, 87),

/*RUGS*/
(303,'Type', false, true, null, 88),
(304,'Rug Length', false, true, 3, 88),
(305,'Rug Width', false, true, 3, 88),
(306,'Condition', false, true, null, 88),

/*INTERIOR_DECORATION*/
(307,'Type', false, true, null, 89),
(308,'Condition', false, true, null, 89),

/*TABLEWARE*/
(309,'Type', false, true, null, 90),
(310,'Condition', false, true, null, 90),

/*COOKING_AND_BAKING*/
(311,'Type', false, true, null, 91),
(312,'Condition', false, true, null, 91),

/*KITCHEN_ACCESSORIES*/
(313,'Condition', false, true, null, 92),

/*BATHROOM_ACCESSORIES*/
(314,'Condition', false, true, null, 93),

/*VIDEO_SURVEILLANCE*/
(315,'Condition', false, true, null, 94),

/*CAR_PARTS*/
(316,'Type', false, true, null, 95),
(317,'Mark', false, true, null, 95),
(318,'Originality', false, true, null, 95),
(319,'Part Side', false, true, null, 95),
(320,'Part Position', false, true, null, 95),
(321,'Condition', false, true, null, 95),

/*WHEELS_AND_TIRES*/
(322,'Type', false, true, null, 96),
(323,'Season', false, true, null, 96),
(324,'Width', false, true, 4, 96),
(325,'Height', false, true, 4, 96),
(326,'Diameter', false, true, 55, 96),
(327,'Condition', false, true, null, 96),

/*RIMS_AND_HUB_CAPS*/
(328,'Type', false, true, null, 97),
(329,'Diameter', false, true, 55, 97),
(330,'Condition', false, true, null, 97),

/*CAR_BATTERIES*/
(331,'Voltage', false, true, 56, 98),
(332,'Capacity', false, true, 28, 98),
(333,'Condition', false, true, null, 98),

/*GAS_EQUIPMENT*/
(334,'Type', false, true, null, 99),
(335,'Condition', false, true, null, 99),

/*OILS_AND_CHEMICALS*/
(336,'Type', false, true, null, 100),

/*CAR_ACCESSORIES*/
(337,'Type', false, true, null, 101),

/*CAR_ELECTRONICS*/
(338,'Type', false, true, null, 102),
(339,'Condition', false, true, null, 102),

/*CAR_AUDIO_AND_VIDEO*/
(340,'Type', false, true, null, 103),
(341,'Condition', false, true, null, 103),

/*PERSONAL_TRANSPORTATION*/
(342,'Type', false, true, null, 104),
(343,'Condition', false, true, null, 104),

/*ATVS_AND_SNOWMOBILES*/
(344,'Type', false, true, null, 105),
(345,'Condition', false, true, null, 105),

/*BOATS_AND_WATER_TRANSPORT*/
(346,'Condition', false, true, null, 106),

/*TRAILERS_AND_BOOTHS*/
(347,'Type', false, true, null, 107),

(348,'Floor Area', false, true, 31, 108),
(349,'Exterior Finish', false, true, null, 108),

/*EVENT_VENUES_RENTAL*/
(350,'Type', false, true, null, 109),
(351,'Floor Area', false, true, null, 109),
(352,'Number of Guests', false, true, null, 109),
(353,'Event Type', false, true, null, 109),
(354,'Facilities', false, true, null, 109),
(355,'Equipment', false, true, null, 109),

(356,'Noise After Hours', false, true, null, 110),
(357,'With Pets', false, true, null, 110)
ON CONFLICT (id) DO NOTHING;