package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum CategoryEnum {

	CARS("Cars", "Автомобили", "Մեքենաներ"),
	TRUCKS("Trucks", "Грузовики", "Բեռնատարներ"),
	BUSES("Buses", "Автобусы", "Ավտոբուսներ"),

	APARTMENTS_BUY("Apartments Buy", "Квартиры Покупка", "Բնակարանների Վաճառք"),
	APARTMENTS_RENTAL("Apartments Rental", "Квартиры Аренда", "Բնակարանների Վարձակալություն"),

	HOUSES_BUY("Houses Buy", "Дома Покупка", "Տների Վաճառք"),
	HOUSES_RENTAL("Houses Rental", "Дома Аренда", "Տների Վարձակալություն"),

	COMMERCIALS_BUY("Commercials Buy", "Коммерческая Недвижимость Покупка", "Կոմերցիոն Անշարժ Գույքի Վաճառք"),
	COMMERCIALS_RENTAL("Commercials Rental", "Коммерческая Недвижимость Аренда", "Կոմերցիոն Անշարժ Գույքի Վարձակալություն"),

	GARAGES_AND_PARKING_BUY("Garages and Parking Buy", "Гаражи и Парковки Покупка", "Ավտոտնակների և Կայանատեղիների Վաճառք"),
	GARAGES_AND_PARKING_RENTAL("Garages and Parking Rental", "Гаражи и Парковки Аренда", "Ավտոտնակների և Կայանատեղիների Վարձակալություն"),

	LANDS_BUY("Lands Buy", "Земли Покупка", "Հողերի Վաճառք"),
	LANDS_RENTAL("Lands Rental", "Земли Аренда", "Հողերի Վարձակալություն"),

	NEW_CONSTRUCTION_APARTMENTS("New Construction Apartments", "Новые Квартиры", "Նորակառույց Բնակարաններ"),
	NEW_CONSTRUCTION_HOUSES("New Construction Houses", "Новые Дома", "Նորակառույց Տներ"),

	APARTMENTS_DAILY_RENTAL("Apartments Daily Rental", "Квартиры на День", "Բնակարաններ Օրավարձով"),
	HOUSES_DAILY_RENTAL("Houses Daily Rental", "Дома на День", "Տներ Օրավարձով"),

	MOBILE_PHONES("Mobile Phones", "Мобильные Телефоны", "Բջջային Հեռախոսներ"),
	NOTEBOOKS("Notebooks", "Ноутбуки", "Նոթբուքեր"),
	COMPUTERS("Computers", "Компьютеры", "Համակարգիչներ"),

	SMART_WATCHES("Smart Watches", "Умные Часы", "Խելացի Ժամացույցներ"),

	TABLETS("Tablets", "Планшеты", "Պլանշետներ"),

	TV_STREAMERS("TV Streamers", "ТВ Стримеры", "Թվային Ընդունիչներ"),

	GAMING_CONSOLES("Gaming Consoles", "игровые Приставки", "Խաղային Համակարգեր"),

	HEADPHONES("Headphones", "Наушники", "Ականջակալներ"),

	COMPUTER_AND_NOTEBOOK_PARTS("Computer and Notebook Parts", "Запчасти для Компьютеров и Ноутбуков ", "Համակարգչային և Նոթբուքի Պահեստամասեր"),

	PHOTO_AND_VIDEO_CAMERAS("Photo and Video Cameras", "Фото и Видеокамеры", "Ֆոտոխցիկներ և Տեսախցիկներ"),

	COMPUTER_GAMES("Computer Games", "Компьютерные игры", "Համակարգչային Խաղեր"),

	SMART_HOME_ACCESSORIES("Smart Home Accessories", "Аксессуары для Умного Дома", "Խելացի Տան Պարագաներ"),

	WASHERS("Washers", "Стиральные Машины", "Լվացքի Մեքենաներ"),

	CLOTHES_DRYERS("Clothes Dryers", "Сушильные Машины", "Հագուստի Չորանոցներ"),

	IRONS_AND_ACCESSORIES("Irons and Accessories", "Утюги и Аксессуары", "Արդուկներ և Պարագաներ"),

	REFRIGERATORS("Refrigerators", "Холодильники", "Սառնարաններ"),

	FREEZERS("Freezers", "Морозильники", "Սառցարաններ"),

	DISHWASHERS("Dishwashers", "Посудомоечные Машины", "Սպասք Լվացող Մեքենաներ"),

	MICROWAVES("Microwaves", "Микроволновки", "Միկրոալիքային Վառարաններ"),

	STOVES("Stoves", "Плиты", "Վառարաններ"),

	COFFEE_MAKERS_AND_ACCESSORIES("Coffee Makers and Accessories", "Кофеварки и Аксессуары", "Սրճեփ Մեքենաներ և Պարագաներ"),

	KETTLES("Kettles", "Чайники", "Թեյնիկներ"),

	RANGE_HOODS("Range Hoods", "Вытяжки", "Օդաքարշ Պահարաններ"),

	VACUUM_CLEANERS("Vacuum Cleaners", "Пылесосы", "Փոշեկուլներ"),

	ROBOTIC_VACUUMS("Robotic Vacuums", "Роботы-Пылесосы", "Ռոբոտ Փոշեկուլներ"),

	FLOOR_WASHERS("Floor Washers", "Поломоечные Машины", "Հատակ Մաքրող Մեքենաներ"),

	AIR_CONDITIONERS("Air Conditioners", "Кондиционеры", "Օդակարգավորիչներ"),

	WATER_HEATERS("Water Heaters", "Водонагреватели", "Ջրատաքացուցիչներ"),

	AIR_PURIFIERS_AND_HUMIDIFIERS("Air Purifiers and Humidifiers", "Очистители и Увлажнители Воздуха", "Օդի Մաքրիչներ և Խոնավեցնողներ"),

	COMPUTERS_PERIPHERALS("Computers Peripherals", "Компьютерные Периферийные Устройства", "Համակարգչային Պերիֆերիկ Սարքեր"),

	AUDIO_PLAYERS_AND_STEREOS("Audio Players and Stereos", "Аудиоплееры и Стереосистемы", "Աուդիո Նվագարկիչներ և Ստերեո Սարքեր"),

	QUADCOPTERS_AND_DRONES("Quadcopters and Drones", "Квадрокоптеры и Дроны", "Քվադրոկոպտերներ և Դրոններ"),

	SOFAS_AND_ARMCHAIRS("Sofas and Armchairs", "Диваны и Кресла", "Բազմոցներ և Բազկաթոռներ"),

	STORAGE("Storage", "Шкафы", "Պահարաններ"),

	TABLES_AND_CHAIRS("Tables and Chairs", "Столы и Стулья", "Սեղաններ և Աթոռներ"),

	BEDROOM_FURNITURE("Bedroom Furniture", "Мебель для Спальни", "Ննջասենյակային Կահույք"),

	KITCHEN_FURNITURE("Kitchen Furniture", "Кухонная Мебель", "Խոհանոցային Կահույք"),

	GARDEN_FURNITURE("Garden Furniture", "Садовая Мебель", "Այգու Կահույք"),

	BARBECUE_AND_ACCESSORIES("Barbecue and Accessories", "Мангалы и Аксессуары", "Մանղալներ և Պարագաներ"),

	GARDEN_DECOR("Garden Decor", "Садовый Декор", "Այգու Զարդարանք"),

	GARDEN_ACCESSORIES("Garden Accessories", "Садовые Аксессуары", "Այգու Պարագաներ"),

	LIGHTING("Lighting", "Освещение", "Լուսավորություն"),

	TEXTILES("Textiles", "Текстиль", "Տեքստիլ"),

	RUGS("Rugs", "Ковры", "Գորգեր"),

	INTERIOR_DECORATION("Interior Decoration", "Интерьерное Украшение", "Ներքին Զարդարանք"),

	TABLEWARE("Tableware", "Посуда", "Սպասք"),

	COOKING_AND_BAKING("Cooking And Baking", "Готовка И Выпечка", "Եփել և Թխել"),

	KITCHEN_ACCESSORIES("Kitchen Accessories", "Кухонные Аксессуары", "Խոհանոցային Պարագաներ"),

	BATHROOM_ACCESSORIES("Bathroom Accessories", "Ванные Аксессуары", "Լոգասենյակի Պարագաներ"),

	VIDEO_SURVEILLANCE("Video Surveillance", "Видеонаблюдение", "Տեսահսկողություն"),

	CAR_PARTS("Car Parts", "Автозапчасти", "Ավտոպահեստամասեր"),

	WHEELS_AND_TIRES("Wheels And Tires", "Колеса И Шины", "Անիվներ և Անվադողեր"),

	RIMS_AND_HUB_CAPS("Rims And Hub Caps", "Диски И Колпаки", "Անվահեծեր և Անվադողի Թասակներ"),

	CAR_BATTERIES("Car Batteries", "Аккумуляторы", "Մարդկոցներ"),

	GAS_EQUIPMENT("Gas Equipment", "Газовое Оборудование", "Գազային Սարքավորումներ"),

	OILS_AND_CHEMICALS("Oils And Chemicals", "Масла И Химия", "Յուղեր և Քիմիկատներ"),

	CAR_ACCESSORIES("Car Accessories", "Автоаксессуары", "Ավտոպարագաներ"),

	CAR_ELECTRONICS("Car Electronics", "Автомобильная Электроника", "Ավտոմոբիլային Էլեկտրոնիկա"),

	CAR_AUDIO_AND_VIDEO("Car Audio And Video", "Автомобильное Аудио И Видео", "Ավտոմոբիլային Աուդիոհամակարգեր"),

	PERSONAL_TRANSPORTATION("Personal Transportation", "Личное Транспортное Средство", "Անձնական Տրանսպորտ"),

	ATVS_AND_SNOWMOBILES("ATVs And Snowmobiles", "Квадроциклы И Снегоходы", "Քվադրոցիկլեր Եվ Ձնագնացներ"),

	BOATS_AND_WATER_TRANSPORT("Boats And Water Transport", "Лодки И Водный Транспорт", "Նավակներ Եվ Ջրային Տրանսպորտ"),

	TRAILERS_AND_BOOTHS("Trailers And Booths", "Прицепы И Кабинки", "Կցորդներ"),

	EVENT_VENUES_RENTAL("Event Venues Rental", "Аренда Мест Для Мероприятий", "Միջոցառումների Անցկացման Վայրերի Վարձակալություն"),

	WOMEN_CLOTHING_BUY("Women Clothing Buy", "Женская Одежда Покупка", "Կանացի Հագուստի Վաճառք"),

	WOMEN_CLOTHING_RENTAL("Women Clothing Rental", "Женская Одежда Аренда", "Կանացի Հագուստի Վարձակալություն"),

	WOMEN_SHOES("Women Shoes", "Женская Обувь", "Կանացի Կոշիկներ"),

	WOMEN_ACCESSORIES("Women Accessories", "Женские Аксессуары", "Կանացի Պարագաներ"),

	MEN_CLOTHING_BUY("Men Clothing Buy", "Мужская Одежда Покупка", "Տղամարդկանց Հագուստի Վաճառք"),

	MEN_SHOES("Men Shoes", "Мужская Обувь", "Տղամարդկանց Կոշիկներ"),

	MEN_ACCESSORIES("Men Accessories", "Мужские Аксессуары", "Տղամարդկանց Պարագաներ"),

	JEWELLERY("Jewellery", "Ювелирные Изделия", "Զարդեր"),

	GLASSES_AND_FRAMES("Glasses And Frames", "Очки И Рамки", "Ակնոցներ Եվ Պատյաններ"),

	WATCHES("Watches", "Часы", "Ժամացույցներ"),

	HANDBAGS_AND_WALLETS("Handbags And Wallets", "Сумки И Кошельки", "Պայուսակներ և Դրամապանակներ"),

	WORKWEAR_AND_ACCESSORIES("Workwear And Accessories", "Рабочая Одежда И Аксессуары", "Աշխատանքային Հագուստ Եվ Պարագաներ"),

	CARNIVAL_COSTUMES("Carnival Costumes", "Карнавальные Костюмы", "Պարահանդեսի Կոստյումներ"),

	WEDDING_DRESSES("Wedding Dresses", "Свадебные Платья", "Հարսանեկան Զգեստներ"),

	WEDDING_SHOES("Wedding Shoes", "Свадебная Обувь", "Հարսանեկան Կոշիկներ"),

	WEDDING_ACCESSORIES("Wedding Accessories", "Свадебные Аксессуары", "Հարսանեկան Պարագաներ"),

	COLLECTIBLE_ITEMS("Collectible Items", "Коллекционные Товары", "Հավաքածուներ"),

	PAINTINGS_AND_PICTURES("Paintings And Pictures", "Картины И Изображения", "Նկարներ"),

	ARTS_OBJECTS("Arts Objects", "Художественные Объекты", "Արվեստի Պարագաներ"),

	ARTS_AND_CRAFTS("Arts And Crafts", "Искусство И Ремесла", "Արհեստ և Ձեռագործություն"),

	MOTORCYCLES("Motorcycles", "Мотоциклы", "Մոտոցիկլեր"),

	MOTORCYCLE_PARTS_AND_ACCESSORIES("Motorcycle Parts And Accessories", "Запчасти И Аксессуары Для Мотоциклов", "Մոտոցիկլի Պահեստամասեր Եվ Պարագաներ"),

	GUITARS("Guitars", "Гитары", "Կիթառներ"),

	PIANOS_AND_KEYBOARD_INSTRUMENTS("Pianos And Keyboard Instruments", "Пианино И Клавишные Инструменты", "Պիանոներ Եվ Դաշնամուրներ"),

	BRASS_AND_WOODWIND_INSTRUMENTS("Brass And Woodwind Instruments", "Медные И Деревянные Инструменты", "Փողային Գործիքներ"),

	STRING_INSTRUMENTS("String Instruments", "Струнные Инструменты", "Լարային Գործիքներ"),

	ACCORDIONS("Accordions", "Аккордеоны", "Ակորդեոններ"),

	DRUMS_AND_PERCUSSION_INSTRUMENTS("Drums And Percussion Instruments", "Барабаны И Ударные Инструменты", "Հարվածային Գործիքներ"),

	STUDIO_ACCESSORIES("Studio Accessories", "Студийные Аксессуары", "Ստուդիայի Պարագաներ"),

	HUNTING_AND_FISHING("Hunting And Fishing", "Охота И Рыбалка", "Որս Եւ Ձկնորսություն"),

	CAMPING_EQUIPMENT("Camping Equipment", "Снаряжение Для Кемпинга", "Զբոսաշրջային Պարագաներ"),

	FITNESS_AND_EXERCISE_EQUIPMENT("Fitness And Exercise Equipment", "Фитнес И Спортивное Оборудование", "Ֆիթնես Եվ Սպորտային Պարագաներ"),

	BILLIARD_AND_BOWLING("Billiard And Bowling", "Бильярд И Боуллинг", "Բիլիարդ Եւ Բոուլինգ"),

	FOOTBALL_AND_BALL_GAMES("Football And Ball Games", "Футбол И Игры с Мячом", "Ֆուտբոլ Եւ Գնդակի Խաղեր"),

	WATER_SPORTS("Water Sports", "Водный спорт", "Ջրային Սպորտ"),

	WINTER_SPORTS_EQUIPMENT("Winter Sports Equipment", "Оборудование Для Зимних Виды Спорта", "Ձմեռային Սպորտի Պարագաներ"),

	BOXING_AND_MARTIAL_ARTS("Boxing And Martial Arts", "Бокс И Боевые Искусства", "Բռնցքամարտ և Մարտարվեստ"),

	TENNIS_AND_BADMINTON("Tennis And Badminton", "Теннис И Бадминтон", "Թենիս Եւ Բադմինթոն"),

	MOUNTAINEERING("Mountaineering", "Альпинизм", "Լեռնագնացություն"),

	SPORTS_NUTRITION("Sports Nutrition", "Спортивное Питание", "Սպորտային Սնունդ"),

	BOOKS_AND_MAGAZINES("Books And Magazines", "Книги И Журналы", "Գրքեր Եւ Ամսագրեր"),

	FILMS_AND_MUSIC("Films And Music", "Фильмы И Музыка", "Ֆիլմեր Եւ Երաժշտություն"),

	DOGS("Dogs", "Собаки", "Շներ"),

	CATS("Cats", "Кошки", "Կատուներ"),

	FISH("Fish", "Рыбы", "Ձկներ"),

	BIRDS("Birds", "Птицы", "Թռչուններ"),

	RODENTS("Rodents", "Грызуны", "Կրծողներ"),

	REPTILES("Reptiles", "Рептилии", "Սողուններ"),

	CATTLE("Cattle", "Коровы и Быки", "Կովեր և Ցլեր"),

	HORSES("Horses", "Лошади", "Ձիեր"),

	PIGS_AND_PIGLETS("Pigs And Piglets", "Свины И Поросенки", "Խոզեր Եւ Խոճկորներ"),

	SHEEP_AND_GOAT("Sheep And Goat", "Овцы И Козы", "Ոչխարներ և Այծեր"),

	RABBITS("Rabbits", "Кролики", "Ճագարներ"),

	GIRLS_CLOTHING("Girls Clothing", "Одежда Для Девочек", "Աղջիկների Հագուստ"),

	BOYS_CLOTHING("Boys Clothing", "Одежда Для Мальчиков", "Տղաների Հագուստ"),

	BABIES_CLOTHING("Babies Clothing", "Одежда Для Младенцев", "Փոքրերի Հագուստ"),

	SHOES_FOR_GIRLS("Shoes For Girls", "Обувь Для Девочек", "Աղջիկների Կոշիկներ"),

	SHOES_FOR_BOYS("Shoes For Boys", "Обувь Для Мальчиков", "Տղաների Կոշիկներ"),

	KIDS_TRANSPORTATION("Kids Transportation", "Транспорт Для Детей", "Երեխաների Տրանսպորտ"),

	BUILDING_SETS("Building Sets", "Конструкторы", "Կոնստրուկտորներ"),

	LEARNING_AND_EDUCATIONAL_TOYS("Learning And Educational Toys", "Развивающие И Образовательные Игрушки", "Ուսումնական Եվ Կրթական Խաղալիքներ"),

	TOYS_FOR_GIRLS("Toys For Girls", "Игрушки Для Девочек", "Աղջիկների Խաղալիքներ"),

	TOYS_FOR_BOYS("Toys For Boys", "Игрушки Для Мальчиков", "Տղաների Խաղալիքներ"),

	TOYS_FOR_NEWBORNS("Toys For Newborns", "Игрушки Для Новорожденных", "Նորածինների Խաղալիքներ"),

	OUTDOORS_AND_SEASONAL_TOYS("Outdoors And Seasonal Toys", "Игрушки Для Улицы И Сезонные Игрушки", "Բակային և Սեզոնային խաղեր"),

	PRODUCTS_FOR_BABIES("Products For Babies", "Продукты Для Младенцев", "Փոքրերի Պարագաներ"),

	STROLLERS("Strollers", "Коляски", "Սայլակներ"),

	CAR_SEATS("Car Seats", "Автокресла", "Ավտոմեքենայի Նստատեղեր"),

	BABY_CARRIERS("Baby Carriers", "Переноски и Кенгуру", "Կրման Հարմարանքներ"),

	WALKERS("Walkers", "Ходунки", "Քայլակներ"),

	SWINGS("Swings", "Качели", "Ճոճաթոռներ"),

	PLAYPENS("Playpens", "Игровые Площадки", "Խաղային Հրապարակներ"),

	BED_ACCESSORIES_AND_DECOR("Bed Accessories And Decor", "Аксессуары И Декор Для Кроватей", "Ննջասենյակի Պարագաներ Եվ Զարդարանքներ"),

	KIDS_FURNITURE("Kids Furniture", "Мебель Для Детей", "Երեխաների Կահույք"),

	HIGH_CHAIRS("High Chairs", "Высокие Стульчики", "Բարձր Աթոռներ"),

	FEEDING("Feeding", "Кормление", "Կերակրում"),

	BATH_AND_HYGIENE("Bath And Hygiene", "Купание И Гигиена", "Լոգանք Եվ Հիգիենա"),

	BACKPACKS_AND_BAGS("Backpacks And Bags", "Рюкзаки И Сумки", "Ուսապարկեր և Պայուսակներ"),

	SCHOOL_SUPPLIES("School Supplies", "Школьные Принадлежности", "Դպրոցական Պարագաներ"),

	PRODUCTS_FOR_DOGS("Products For Dogs", "Продукты Для Собак", "Շների Պարագաներ"),

	PRODUCTS_FOR_CATS("Products For Cats", "Продукты Для Кошек", "Կատուների Պարագաներ"),

	PRODUCTS_FOR_FISH_AND_REPTILES("Products For Fish And Reptiles", "Продукты Для Рыб И Рептилий", "Ձկների և Սողունների Պարագաներ"),

	PRODUCTS_FOR_RODENTS("Products For Rodents", "Продукты Для Грызунов", "Կրծողների Պարագաներ"),

	PRODUCTS_FOR_FARM_ANIMALS("Products For Farm Animals", "Продукты Для Сельскохозяйственных Животных", "Գյուղատնտեսական Կենդանիների Պարագաներ"),

	PRODUCTS_FOR_BIRDS("Products For Birds", "Продукты Для Птиц", "Թռչունների Պարագաներ"),

	HAND_TOOLS("Hand Tools", "Ручные Инструменты", "Ձեռքի Գործիքներ"),

	MACHINE_TOOLS("Machine Tools", "Станочные Инструменты", "Հաստոցներ"),

	ELECTRICAL_DEVICES("Electrical Devices", "Электрические Приборы", "Էլեկտրական Սարքեր"),

	MEASURING_EQUIPMENT("Measuring Equipment", "Измерительное Оборудование", "Չափիչ Սարքեր"),

	SAWS("Saws", "Пилы", "Սղոցներ"),

	WELDING_EQUIPMENT("Welding Equipment", "Сварочное Оборудование", "Զոդող Սարքեր"),

	CONCRETE_MIXERS("Concrete Mixers", "Бетономешалки", "Բետոնախառնիչներ"),

	LADDERS_AND_STEPLADDERS("Ladders And Stepladders", "Лестницы И Стремянки", "Սանդուղքներ և Աստիճանասանդուղքներ"),

	SCAFFOLDING_AND_TOWERS("Scaffolding And Towers", "Строительные Лестницы И Вышки", "Փայտամածեր և Աշտարակներ"),

	PROTECTIVE_EQUIPMENT("Protective Equipment", "Защитное Оборудование", "Պաշտպանիչ Պարագաներ"),

	GARDENING_EQUIPMENT("Gardening Equipment", "Садовое Техника", "Այգեգործության Սարքեր"),

	FAUCETS("Faucets", "Краны", "Ծորակներ"),

	SHOWERS_AND_BATHS("Showers And Baths", "Души И Ванны", "Ցնցուղներ և Լոգարաններ"),

	SINKS_AND_WASHBASINS("Sinks And Washbasins", "Раковины И Умывальники", "Լվացարաններ"),

	PUMPS("Pumps", "Насосы", "Պոմպեր"),

	WATER_METERS("Water Meters", "Счетчики Воды", "Ջրաչափեր"),

	TOILETS_AND_BIDETS("Toilets And Bidets", "Унитазы И Биде", "Զուգարաններ Եւ Բիդեներ"),

	WINDOWS("Windows", "Окна", "Պատուհաններ"),

	DOORS("Doors", "Двери", "Դռներ"),

	GATES_AND_FENCES("Gates And Fences", "Ворота И Заборы", "Դարպասներ և Պարիսպներ"),

	FLOORING("Flooring", "Покрытия Для Полов", "Հատակի Ծածկույթ"),

	WATER_SUPPLY_AND_PIPES("Water Supply And Pipes", "Водоснабжение И Трубы", "Ջրամատակարարում և Խողովակներ"),

	RETAIL_AND_SHOPS_EQUIPMENT("Retail And Shops Equipment", "Оборудование Для Розничной Торговли И Магазинов", "Առևտրային Պարագաներ"),

	MANUFACTURING_EQUIPMENT("Manufacturing Equipment", "Производственное Оборудование", "Արտադրական Սարքեր"),

	OFFICE_EQUIPMENT("Office Equipment", "Офисное Оборудование", "Գրասենյակի Պարագաներ"),

	RESTAURANTS_AND_CAFES_EQUIPMENT("Restaurants And Cafes Equipment", "Оборудование Для Ресторанов И Кафе", "Ռեստորանների և Սրճարանների Պարագաներ"),

	BEAUTY_SALONS_EQUIPMENT("Beauty Salons Equipment", "Оборудование Для Салонов Красоты", "Գեղեցկության Սրահների Պարագաներ"),

	CAR_SERVICES_EQUIPMENT("Car Services Equipment", "Оборудование Для Автосервисов", "Ավտոսպասարկման Կենտրենների Պարագաներ"),

	WAREHOUSE_EQUIPMENT("Warehouse Equipment", "Складское Оборудование", "Պահեստարանների Պարագաներ"),

	AGRICULTURAL_EQUIPMENT("Agricultural Equipment", "Сельскохозяйственное Оборудование", "Գյուղատնտեսական Սարքեր"),

	ATTRACTIONS_AND_VENDING_MACHINES("Attractions And Vending Machines", "Аттракционы И Торговые Автоматы", "Զվարճանքներ և Վաճառքի Ավտոմատներ"),

	ADVERTISING_AND_EXHIBITION_EQUIPMENT("Advertising And Exhibition Equipment", "Оборудование Для Рекламы И Выставок", "Գովազդային և Ցուցահանդեսային Պարագաներ"),

	BUSINESSES_SALE("Businesses Sale", "Продажа Бизнесов", "Բիզնեսի Վաճառք"),
	BUSINESSES_RENTAL("Businesses Rental", "Аренда Бизнесов", "Բիզնեսի Վարձակալություն");


	private final String english;
	private final String russian;
	private final String armenian;

	CategoryEnum(String english, String russian, String armenian) {
		this.english = english;
		this.russian = russian;
		this.armenian = armenian;
	}

}
