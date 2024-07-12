package am.devvibes.buyandsell.util;

import am.devvibes.buyandsell.entity.mobile.MobilePhoneMarkEntity;
import lombok.Getter;

@Getter
public enum CategoryEnum {

	CARS("Cars"),
	TRUCKS("Trucks"),
	BUSES("Buses"),

	APARTMENTS_BUY("Apartments Buy"),
	APARTMENTS_RENTAL("Apartments Rental"),

	HOUSES_BUY("Houses Buy"),
	HOUSES_RENTAL("Houses Rental"),

	COMMERCIALS_BUY("Commercials Buy"),
	COMMERCIALS_RENTAL("Commercials Rental"),

	GARAGES_AND_PARKING_BUY("Garages and Parking Buy"),
	GARAGES_AND_PARKING_RENTAL("Garages and Parking Rental"),

	LANDS_BUY("Lands Buy"),
	LANDS_RENTAL("Lands Rental"),

	NEW_CONSTRUCTION_APARTMENTS("New Construction Apartments"),
	NEW_CONSTRUCTION_HOUSES("New Construction Houses"),

	APARTMENTS_DAILY_RENTAL("Apartments Daily Rental"),
	HOUSES_DAILY_RENTAL("Houses Daily Rental"),

	MOBILE_PHONES("Mobile Phones"),
	NOTEBOOKS("Notebooks"),
	COMPUTERS("Computers"),

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

	BOXING_AND_MARTIAL_ARTS("Boxing and Martial Arts");


	private final String name;

	CategoryEnum(String name) {
		this.name = name;
	}

}
