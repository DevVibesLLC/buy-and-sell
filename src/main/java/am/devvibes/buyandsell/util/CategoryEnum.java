package am.devvibes.buyandsell.util;

import am.devvibes.buyandsell.entity.mobile.MobilePhoneMarkEntity;
import lombok.Getter;

@Getter
public enum CategoryEnum {

	CAR("Car"),
	TRUCK("Truck"),
	BUS("Bus"),

	APARTMENT_BUY("Apartment Buy"),
	APARTMENT_RENTAL("Apartment Rental"),

	HOUSE_BUY("House Buy"),
	HOUSE_RENTAL("House Rental"),

	COMMERCIAL_BUY("Commercial Buy"),
	COMMERCIAL_RENTAL("Commercial Rental"),

	GARAGE_AND_PARKING_BUY("Garage and Parking Buy"),
	GARAGE_AND_PARKING_RENTAL("Garage and Parking Rental"),

	LAND_BUY("Land Buy"),
	LAND_RENTAL("Land Rental"),

	NEW_CONSTRUCTION_APARTMENT("New Construction Apartment"),
	NEW_CONSTRUCTION_HOUSE("New Construction House"),

	APARTMENT_DAILY_RENTAL("Apartment Daily Rental"),
	HOUSE_DAILY_RENTAL("House Daily Rental"),

	MOBILE_PHONE("Mobile Phone"),
	NOTEBOOK("Notebook"),
	COMPUTER("Computers"),

	SMART_WATCH("Smart Watch"),

	TABLET("Tablet"),

	TV("TV"),

	GAMING_CONSOLE("Gaming Console"),

	HEADPHONE("Headphones"),

	COMPUTER_AND_NOTEBOOK_PARTS("Computer and Notebook Parts"),

	PHOTO_AND_VIDEO_CAMERA("Photo and Video Camera"),

	COMPUTER_GAMES("Computer Games"),

	SMART_HOME_ACCESSORIES("Smart Home Accessories"),

	WASHER("Washer"),

	CLOTHES_DRYER("Clones Dryer"),

	IRON_AND_ACCESSORIES("Iron and Accessories");
	private final String name;

	CategoryEnum(String name) {
		this.name = name;
	}

}
