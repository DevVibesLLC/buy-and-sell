INSERT INTO measurement_entity (id, symbol, category)
VALUES
(1, 'm', 'Length'),
(2, 'km', 'Length'),
(3, 'cm', 'Length'),
(4, 'mm', 'Length'),
(5, 'µm', 'Length'),
(6, 'nm', 'Length'),
(7, 'mi', 'Length'),
(8, 'yd', 'Length'),
(9, 'ft', 'Length'),
(10, 'in', 'Length'),
(11, 'kg', 'Mass'),
(12, 'g', 'Mass'),
(13, 'mg', 'Mass'),
(14, 'µg', 'Mass'),
(15, 't', 'Mass'),
(16, 'lb', 'Mass'),
(17, 'oz', 'Mass'),
(18, 's', 'Time'),
(19, 'ms', 'Time'),
(20, 'µs', 'Time'),
(21, 'ns', 'Time'),
(22, 'min', 'Time'),
(23, 'h', 'Time'),
(24, 'd', 'Time'),
(25, '°C', 'Temperature'),
(26, '°F', 'Temperature'),
(27, 'K', 'Temperature'),
(28, 'A', 'Electric current'),
(29, 'mA', 'Electric current'),
(30, 'µA', 'Electric current'),
(31, 'm²', 'Area'),
(32, 'km²', 'Area'),
(33, 'ha', 'Area'),
(34, 'mi²', 'Area'),
(35, 'yd²', 'Area'),
(36, 'ft²', 'Area'),
(37, 'in²', 'Area'),
(38, 'ac', 'Area'),
(39, 'm³', 'Volume'),
(40, 'L', 'Volume'),
(41, 'mL', 'Volume'),
(42, 'cm³', 'Volume'),
(43, 'in³', 'Volume'),
(44, 'ft³', 'Volume'),
(45, 'm/s', 'Speed'),
(46, 'km/h', 'Speed'),
(47, 'mph', 'Speed'),
(52, 'W', 'Power'),
(53, 'kW', 'Power'),
(54, 'hp', 'Power'),
(55, 'R', 'Wheel'),
(56, 'Volt', 'Voltage')
ON CONFLICT (id) DO NOTHING;