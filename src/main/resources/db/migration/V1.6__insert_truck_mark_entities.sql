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
(30,'John Deere', 2),
(31,'Kamaz (Камаз)', 2),
(32,'Karry', 2),
(33,'Kia', 2),
(34,'LiuGong', 2),
(35,'Man', 2),
(36,'MAZ (Маз)', 2),
(37,'Mazda', 2),
(38,'Mercedes-Benz', 2),
(39,'Mitsubishi', 2),
(40,'Mudan', 2),
(41,'Naveco', 2),
(42,'Nissan', 2),
(43,'Paus', 2),
(44,'Renault', 2),
(45,'Runhorse', 2),
(46,'Saurer', 2),
(47,'Scania', 2),
(48,'Shacman (Shaanxi)', 2),
(49,'Silant', 2),
(50,'Sinotruk', 2),
(51,'SITRAK', 2),
(52,'Skoda', 2),
(53,'Skoda LIAZ', 2),
(54,'Star', 2),
(55,'Studebaker', 2),
(56,'Tatra', 2),
(57,'Tonly', 2),
(58,'Toyota', 2),
(59,'UAZ (Уаз)', 2),
(60,'Volkswagen', 2),
(61,'Volvo', 2),
(62,'Wuling', 2),
(63,'XCMG', 2),
(64,'Yuejin', 2),
(65,'ZIL (ЗИЛ)', 2),
(66,'ZIS (ЗИС)', 2),
(67,'Вваз', 2),
(68,'ГВА', 2),
(69,'Граз', 2),
(70,'КАЗ', 2),
(71,'КС ДОН', 2),
(72,'КрАЗ', 2),
(73,'МАЗ-МАН', 2),
(74,'Планета', 2),
(75,'Русич (КЗКТ)', 2),
(76,'Тонар', 2),
(77,'УЗСТ', 2),
(78,'Урал', 2),
(79,'ЯРОВИТ МОТОРС', 2)
ON CONFLICT (id) DO NOTHING;