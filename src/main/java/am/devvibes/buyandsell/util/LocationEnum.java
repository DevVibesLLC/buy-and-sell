package am.devvibes.buyandsell.util;

import am.devvibes.buyandsell.exception.NotFoundException;
import lombok.Getter;

@Getter
public enum LocationEnum {

	ARMENIA(1L,"Armenia",null),
		ARAGATSOTN(2L,"Aragatsotn",ARMENIA),
		ARARAT(3L,"Ararat",ARMENIA),
		ARMAVIR(4L,"Armavir",ARMENIA),
		GEGHARKUNIK(5L,"Gegharkunik",ARMENIA),
		KOTAYK(6L,"Kotayk",ARMENIA),
		LORI(7L,"Lori",ARMENIA),
		SHIRAK(8L,"Shirak",ARMENIA),
		SYUNIK(9L,"Syunik",ARMENIA),
		TAVUSH(10L,"Tavush",ARMENIA),
		VAYOTS_DZOR(11L,"Vayots Dzor",ARMENIA),
		YEREVAN(12L,"Yerevan",ARMENIA),
			NOR_NORK(13L,"Nor Nork",YEREVAN),
			ARABKIR(14L,"Arabkir",YEREVAN),
			KENTRON(15L,"Kentron",YEREVAN),
			SHENGAVIT(16L,"Shengavit",YEREVAN),
			EREBUNI(17L,"Erebuni",YEREVAN);

	private final Long id;
	private final String value;
	private final LocationEnum parent;


	LocationEnum(Long id, String value, LocationEnum parent) {
		this.id = id;
		this.value = value;
		this.parent = parent;
	}

	private static final LocationEnum[] locations = LocationEnum.values();

	public static LocationEnum getCity(Long locationId) {
		return getLocationById(locationId);
	}

	public static LocationEnum getRegion(Long locationId) {
		return getLocationById(locationId).getParent();
	}

	public static LocationEnum getCountry( Long locationId) {
		return getLocationById(locationId).getParent().getParent();
	}

	private static LocationEnum getLocationById(Long id) {
		for (LocationEnum location : LocationEnum.values()) {
			if (location.getId().equals(id)) {
				return location;
			}
		}
		throw new NotFoundException(ExceptionConstants.LOCATION_NOT_FOUND);
	}

}
