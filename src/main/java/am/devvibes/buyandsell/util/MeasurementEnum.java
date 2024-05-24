package am.devvibes.buyandsell.util;

import lombok.Getter;

@Getter
public enum MeasurementEnum {
    METER(1L, "m", "Length"),
    KILOMETER(2L, "km", "Length"),
    CENTIMETER(3L, "cm", "Length"),
    MILLIMETER(4L, "mm", "Length"),
    MICROMETER(5L, "µm", "Length"),
    NANOMETER(6L, "nm", "Length"),
    MILE(7L, "mi", "Length"),
    YARD(8L, "yd", "Length"),
    FOOT(9L, "ft", "Length"),
    INCH(10L, "in", "Length"),

    KILOGRAM(11L, "kg", "Mass"),
    GRAM(12L, "g", "Mass"),
    MILLIGRAM(13L, "mg", "Mass"),
    MICROGRAM(14L, "µg", "Mass"),
    TON(15L, "t", "Mass"),
    POUND(16L, "lb", "Mass"),
    OUNCE(17L, "oz", "Mass"),

    SECOND(18L, "s", "Time"),
    MILLISECOND(19L, "ms", "Time"),
    MICROSECOND(20L, "µs", "Time"),
    NANOSECOND(21L, "ns", "Time"),
    MINUTE(22L, "min", "Time"),
    HOUR(23L, "h", "Time"),
    DAY(24L, "d", "Time"),

    CELSIUS(25L, "°C", "Temperature"),
    FAHRENHEIT(26L, "°F", "Temperature"),
    KELVIN(27L, "K", "Temperature"),

    AMPERE(28L, "A", "Electric current"),
    MILLIAMPERE(29L, "mA", "Electric current"),
    MICROAMPERE(30L, "µA", "Electric current"),

    SQUARE_METER(31L, "m²", "Area"),
    SQUARE_KILOMETER(32L, "km²", "Area"),
    HECTARE(33L, "ha", "Area"),
    SQUARE_MILE(34L, "mi²", "Area"),
    SQUARE_YARD(35L, "yd²", "Area"),
    SQUARE_FOOT(36L, "ft²", "Area"),
    SQUARE_INCH(37L, "in²", "Area"),
    ACRE(38L, "ac", "Area"),

    CUBIC_METER(39L, "m³", "Volume"),
    LITER(40L, "L", "Volume"),
    MILLILITER(41L, "mL", "Volume"),
    CUBIC_CENTIMETER(42L, "cm³", "Volume"),
    CUBIC_INCH(43L, "in³", "Volume"),
    CUBIC_FOOT(44L, "ft³", "Volume"),

    METER_PER_SECOND(45L, "m/s", "Speed"),
    KILOMETER_PER_HOUR(46L, "km/h", "Speed"),
    MILE_PER_HOUR(47L, "mph", "Speed"),

    WATT(52L, "W", "Power"),
    KILOWATT(53L, "kW", "Power"),
    HORSEPOWER(54L, "hp", "Power");

    private final Long id;
    private final String symbol;
    private final String category;

    MeasurementEnum(Long id,String symbol, String category) {
        this.id = id;
        this.symbol = symbol;
        this.category = category;
    }

}
