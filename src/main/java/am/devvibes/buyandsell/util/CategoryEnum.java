package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum CategoryEnum {

	CARS("Cars"), TRUCKS("Trucks"), BUSES("Buses"),

	APARTMENTS_BUY("Apartments Buy"), APARTMENTS_RENTAL("Apartments Rental"),

	HOUSES_BUY("Houses Buy"), HOUSES_RENTAL("Houses Rental"),

	COMMERCIALS_BUY("Commercials Buy"), COMMERCIALS_RENTAL("Commercials Rental"),

	GARAGES_AND_PARKING_BUY("Garages and Parking Buy"), GARAGES_AND_PARKING_RENTAL("Garages and Parking Rental"),

	LANDS_BUY("Lands Buy"), LANDS_RENTAL("Lands Rental"),

	NEW_CONSTRUCTION_APARTMENTS("New Construction Apartments"), NEW_CONSTRUCTION_HOUSES("New Construction Houses"),

	APARTMENTS_DAILY_RENTAL("Apartments Daily Rental"), HOUSES_DAILY_RENTAL("Houses Daily Rental"),

	MOBILE_PHONES("Mobile Phones"), NOTEBOOKS("Notebooks"), COMPUTERS("Computers"),

	SMART_WATCHES("Smart Watches"),

	TABLETS("Tablets"),

	TV_STREAMERS("TV Streamers"),

	GAMING_CONSOLES("Gaming Consoles"),

	HEADPHONES("Headphones"),

	COMPUTER_AND_NOTEBOOK_PARTS("Computer and Notebook Parts"),

	PHOTO_AND_VIDEO_CAMERAS("Photo and Video Cameras"),

	COMPUTER_GAMES("Computer Games"),

	SMART_HOME_ACCESSORIES("Smart Home Accessories"),

	WASHERS("Washers"),

	CLOTHES_DRYERS("Clothes Dryers"),

	IRONS_AND_ACCESSORIES("Irons and Accessories"),

	REFRIGERATORS("Refrigerators"),

	FREEZERS("Freezers"),

	DISHWASHERS("Dishwashers"),

	MICROWAVES("Microwaves"),

	STOVES("Stoves"),

	COFFEE_MAKERS_AND_ACCESSORIES("Coffee Makers and Accessories"),

	KETTLES("Kettles"),

	RANGE_HOODS("Range Hoods"),

	VACUUM_CLEANERS("Vacuum Cleaners"),

	ROBOTIC_VACUUMS("Robotic Vacuums"),

	FLOOR_WASHERS("Floor Washers"),

	AIR_CONDITIONERS("Air Conditioners"),

	WATER_HEATERS("Water Heaters"),

	AIR_PURIFIERS_AND_HUMIDIFIERS("Air Purifiers and Humidifiers"),

	COMPUTERS_PERIPHERALS("Computers Peripherals"),

	AUDIO_PLAYERS_AND_STEREOS("Audio Players and Stereos"),

	QUADCOPTERS_AND_DRONES("Quadcopters and Drones"),

	SOFAS_AND_ARMCHAIRS("Sofas and Armchairs"),

	STORAGE("Storage"),

	TABLES_AND_CHAIRS("Tables and Chairs"),

	BEDROOM_FURNITURE("Bedroom Furniture"),

	KITCHEN_FURNITURE("Kitchen Furniture"),

	GARDEN_FURNITURE("Garden Furniture"),

	BARBECUE_AND_ACCESSORIES("Barbecue and Accessories"),

	GARDEN_DECOR("Garden Decor"),

	GARDEN_ACCESSORIES("Garden Accessories"),

	LIGHTING("Lighting"),

	TEXTILES("Textiles"),

	RUGS("Rugs"),

	INTERIOR_DECORATION("Interior Decoration"),

	TABLEWARE("Tableware"),

	COOKING_AND_BAKING("Cooking and Baking"),

	KITCHEN_ACCESSORIES("Kitchen Accessories"),

	BATHROOM_ACCESSORIES("Bathroom Accessories"),

	VIDEO_SURVEILLANCE("Video Surveillance"),

	CAR_PARTS("Car Parts"),

	WHEELS_AND_TIRES("Wheels and Tires"),

	RIMS_AND_HUB_CAPS("Rims and Hub Caps"),

	CAR_BATTERIES("Car Batteries"),

	GAS_EQUIPMENT("Gas Equipment"),

	OILS_AND_CHEMICALS("Oils And Chemicals"),

	CAR_ACCESSORIES("Car Accessories"),

	CAR_ELECTRONICS("Car Electronics"),

	CAR_AUDIO_AND_VIDEO("Car Audio and Video"),

	PERSONAL_TRANSPORTATION("Personal Transportation"),

	ATVS_AND_SNOWMOBILES("ATVs and Snowmobiles"),

	BOATS_AND_WATER_TRANSPORT("Boats and Water Transport"),

	TRAILERS_AND_BOOTHS("Trailers and Booths"),

	EVENT_VENUES_RENTAL("Event Venues Rental"),

	WOMEN_CLOTHING_BUY("Women Clothing Buy"),

	WOMEN_CLOTHING_RENTAL("Women Clothing Rental"),

	WOMEN_SHOES("Women Shoes"),

	WOMEN_ACCESSORIES("Women Accessories"),

	MEN_CLOTHING_BUY("Men Clothing Buy"),

	MEN_SHOES("Women Shoes"),

	MEN_ACCESSORIES("Women Accessories"),

	JEWELLERY("Jewellery"),

	GLASSES_AND_FRAMES("Glasses and Frames"),

	WATCHES("Watches"),

	HANDBAGS_AND_WALLETS("Handbags and Wallets"),

	WORKWEAR_AND_ACCESSORIES("Workwear and Accessories"),

	CARNIVAL_COSTUMES("Carnival Costumes"),

	WEDDING_DRESSES("Wedding Dresses"),

	WEDDING_SHOES("Wedding Shoes"),

	WEDDING_ACCESSORIES("Wedding Accessories"),

	COLLECTIBLE_ITEMS("Collectible Items"),

	PAINTINGS_AND_PICTURES("Paintings and Pictures"),

	ARTS_OBJECTS("Arts Objects"),

	ARTS_AND_CRAFTS("Arts and Crafts"),

	MOTORCYCLES("Motorcycles"),

	MOTORCYCLE_PARTS_AND_ACCESSORIES("Motorcycles and Accessories"),

	GUITARS("Guitars"),

	PIANOS_AND_KEYBOARD_INSTRUMENTS("Pianos and Keyboard Instruments"),

	BRASS_AND_WOODWIND_INSTRUMENTS("Brass and Woodwind Instruments"),

	STRING_INSTRUMENTS("String Instruments"),

	ACCORDIONS("Accordions"),

	DRUMS_AND_PERCUSSION_INSTRUMENTS("Drums and Percussion Instruments"),

	STUDIO_ACCESSORIES("Studio Accessories"),

	HUNTING_AND_FISHING("Hunting and Fishing"),

	CAMPING_EQUIPMENT("Camping Equipment"),

	FITNESS_AND_EXERCISE_EQUIPMENT("Fitness and Exercise Equipment"),

	BILLIARD_AND_BOWLING("Billiard and Bowling"),

	FOOTBALL_AND_BALL_GAMES("Football and Ball Games"),

	WATER_SPORTS("Water Sports"),

	WINTER_SPORTS_EQUIPMENT("Winter Sports Equipment"),

	BOXING_AND_MARTIAL_ARTS("Boxing and Martial Arts"),

	TENNIS_AND_BADMINTON("Tennis and Badminton"),

	MOUNTAINEERING("Mountaineering"),

	SPORTS_NUTRITION("Sports Nutrition"),

	BOOKS_AND_MAGAZINES("Books and Magazines"),

	FILMS_AND_MUSIC("Films and Music"),

	DOGS("Dogs"),

	CATS("Cats"),

	FISH("Fish"),

	BIRDS("Birds"),

	RODENTS("Rodents"),

	REPTILES("Reptiles"),

	CATTLE("Cattle"),

	HORSES("Horses"),

	PIGS_AND_PIGLETS("Pigs and Piglets"),

	SHEEP_AND_GOAT("Sheep and Goat"),

	RABBITS("Rabbits"),

	GIRLS_CLOTHING("Girls Clothing"),

	BOYS_CLOTHING("Boys Clothing"),

	BABIES_CLOTHING("Babies Clothing"),

	SHOES_FOR_GIRLS("Shoes for Girls"),

	SHOES_FOR_BOYS("Shoes for Boys"),

	KIDS_TRANSPORTATION("Kids Transportation"),

	BUILDING_SETS("Building Sets"),

	LEARNING_AND_EDUCATIONAL_TOYS("Learning and Educational Toys"),

	TOYS_FOR_GIRLS("Toys for Girls"),

	TOYS_FOR_BOYS("Toys for Boys"),

	TOYS_FOR_NEWBORNS("Toys for Newborns"),

	OUTDOORS_AND_SEASONAL_TOYS("Outdoors and Seasonal Toys"),

	PRODUCTS_FOR_BABIES("Product for Babies"),

	STROLLERS("Strollers"),

	CAR_SEATS("Car Seats"),

	BABY_CARRIERS("Baby Carriers"),

	WALKERS("Walkers"),

	SWINGS("Swings"),

	PLAYPENS("Playpens"),

	BED_ACCESSORIES_AND_DECOR("Bed Accessories and Decor"),

	KIDS_FURNITURE("Kids Furniture"),

	HIGH_CHAIRS("High Chairs"),

	FEEDING("Feeding"),

	BATH_AND_HYGIENE("Bath and Hygiene"),

	BACKPACKS_AND_BAGS("Backpacks and Bags"),

	SCHOOL_SUPPLIES("School Supplies"),

	PRODUCTS_FOR_DOGS("Products for Dogs"),

	PRODUCTS_FOR_CATS("Products for Cats"),

	PRODUCTS_FOR_FISH_AND_REPTILES("Products for Fish and Reptiles"),

	PRODUCTS_FOR_RODENTS("Products for Rodents"),

	PRODUCTS_FOR_FARM_ANIMALS("Products for Farm Animals"),

	PRODUCTS_FOR_BIRDS("Products for Birds"),

	HAND_TOOLS("Hand Tools"),

	MACHINE_TOOLS("Machine Tools"),

	ELECTRICAL_DEVICES("Electrical Devices"),

	MEASURING_EQUIPMENT("Measuring Equipment"),

	SAWS("Saws"),

	WELDING_EQUIPMENT("Welding Equipment"),

	CONCRETE_МIXERS("Concrete Мixers"),

	LADDERS_AND_STEPLADDERS("Ladders and Stepladders"),

	SCAFFOLDING_AND_TOWERS("Scaffolding and Towers"),

	PROTECTIVE_EQUIPMENT("Protective Equipment"),

	GARDENING_EQUIPMENT("Gardening Equipment"),

	FAUCETS("Faucets"),

	SHOWERS_AND_BATHS("Showers and Baths"),

	SINKS_AND_WASHBASINS("Sinks and Washbasins"),

	PUMPS("Pumps"),

	WATER_METERS("Water Meters"),

	TOILETS_AND_BIDETS("Toilets and Bidets"),

	WINDOWS("Windows"),

	DOORS("Doors"),

	GATES_AND_FENCES("Gates and Fences"),

	FLOORING("Flooring"),

	WATER_SUPPLY_AND_PIPES("Water Supply and Pipes"),

	RETAIL_AND_SHOPS_EQUIPMENT("Retail and Shops Equipment"),

	MANUFACTURING_EQUIPMENT("Manufacturing Equipment"),

	OFFICE_EQUIPMENT("Office Equipment"),

	RESTAURANTS_AND_CAFES_EQUIPMENT("Restaurants and Cafes Equipment"),

	BEAUTY_SALONS_EQUIPMENT("Beauty Salons Equipment"),

	CAR_SERVICES_EQUIPMENT("Car Services Equipment"),

	WAREHOUSE_EQUIPMENT("Warehouse Equipment"),

	AGRICULTURAL_EQUIPMENT("Agricultural Equipment"),

	ATTRACTIONS_AND_VENDING_MACHINES("Attractions and Vending Machines"),

	ADVERTISING_AND_EXHIBITION_EQUIPMENT("Advertising and Exhibition Equipment"),

	BUSINESSES_SALE("Businesses Sale"), BUSINESSES_RENTAL("Businesses Rental");

	private final String name;

	CategoryEnum(String name) {
		this.name = name;
	}

}
