INSERT INTO truck_mark_entity (id, name, category_id)
VALUES
(1,'Altkam', 2),
(2,'ASTRA', 2),
(3,'Atlant', 2),
(4,'Avia', 2),
(5,'BAW', 2),
(6,'BelAZ (Белаз)', 2),
(7,'Bell', 2),
(8,'Changan', 2),
(9, 'Daewoo', 2),
(10,'DAF', 2),
(11,'Dali', 2),
(12,'Dayun', 2),
(13,'DongFeng', 2),
(14,'FAUN', 2),
(15,'FAW', 2),
(16,'Fiat Ducato', 2),
(17,'Ford', 2),
(18,'Foton', 2),
(19,'Freightliner', 2),
(20,'GAZ (Газ)', 2),
(21,'GMC', 2),
(22,'Hino', 2),
(23,'Howo', 2),
(24,'Hyundai', 2),
(25,'IFA', 2),
(26,'Isuzu', 2),
(27,'IVECO', 2),
(28,'JAC', 2),
(29,'JMC', 2),
(30,'Kamaz (Камаз)', 2),
(31,'Karry', 2),
(32,'Kia', 2),
(33,'LiuGong', 2),
(34,'Man', 2),
(35,'MAZ (Маз)', 2),
(36,'Mazda', 2),
(37,'Mercedes-Benz', 2),
(38,'Mitsubishi', 2),
(39,'Naveco', 2),
(40,'Nissan', 2),
(41,'Renault', 2),
(42,'Runhorse', 2),
(43,'Scania', 2),
(44,'Shacman (Shaanxi)', 2),
(45,'Sinotruk', 2),
(46,'SITRAK', 2),
(47,'Tatra', 2),
(48,'Toyota', 2),
(49,'UAZ (Уаз)', 2),
(50,'Volkswagen', 2),
(51,'Volvo', 2),
(52,'Wuling', 2),
(53,'XCMG', 2),
(54,'Yuejin', 2),
(55,'ZIL (ЗИЛ)', 2),
(56,'ZIS (ЗИС)', 2),
(57,'Вваз', 2),
(58,'Граз', 2),
(59,'КАЗ', 2),
(60,'КрАЗ', 2),
(61,'МАЗ-МАН', 2),
(62,'Тонар', 2),
(63,'УЗСТ', 2),
(64,'Урал', 2)
ON CONFLICT (id) DO NOTHING;