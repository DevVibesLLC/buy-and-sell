package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum DescriptionNameEnum {

	SPECIFICATIONS("Specifications", "Характеристики", "Բնութագրեր"),

	EXTERIOR("Exterior", "Экстерьер", "Էքստերիեր"),

	INTERIOR("Interior", "Интерьер", "Ինտերիեր"),

	ADDITIONAL_INFORMATION("Additional Information", "Дополнительная Информация", "Լրացուցիչ Տեղեկություններ"),

	BUILDING_INFORMATION("Building Information", "Информация о Здании", "Տեղեկություններ Շենքի Մասին"),

	APARTMENT_INFORMATION("Apartment Information", "Информация о Квартире", "Տեղեկություններ Բնակարանի Մասին"),

	HOUSE_RULES("House Rules", "Домашние Правила", "Բնակության Կանոններ"),

	DEAL_TERMS("Deal Terms", "Условия Сделки", "Գործարքի Պայմաններ"),

	HOUSE_INFORMATION("House Information", "Информация о Доме", "Տեղեկություններ Տան Մասին"),

	LOT_INFORMATION("Lot Information", "Информация о Лоте", "Տեղեկություններ Հողատարածքի Մասին"),

	STAGE_OF_PREPARATION("Stage of Preparation", "Стадия Подготовки", "Պատրաստության Փուլ");

	private final String english;
	private final String russian;
	private final String armenian;

	DescriptionNameEnum(String english, String russian, String armenian) {
		this.english = english;
		this.russian = russian;
		this.armenian = armenian;
	}

}