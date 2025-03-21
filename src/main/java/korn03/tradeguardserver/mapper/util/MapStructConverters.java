package korn03.tradeguardserver.mapper.util;

import org.mapstruct.Named;
import java.math.BigDecimal;

/**
 * Utility class containing reusable MapStruct type converters.
 * These converters can be used across different mappers by using the @Named qualifier.
 */
public class MapStructConverters {

    private MapStructConverters() {
    }

    @Named("stringToBigDecimal")
    public static BigDecimal stringToBigDecimal(String value) {
        return (value != null && !value.isEmpty()) ? new BigDecimal(value) : null;
    }

    @Named("stringToInteger")
    public static Integer stringToInteger(String value) {
        return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : null;
    }

    @Named("stringToBoolean")
    public static Boolean stringToBoolean(String value) {
        return (value != null) ? Boolean.parseBoolean(value) : null;
    }

    @Named("stringToLong")
    public static Long stringToLong(String value) {
        return (value != null && !value.isEmpty()) ? Long.parseLong(value) : null;
    }

    @Named("stringToDouble")
    public static Double stringToDouble(String value) {
        return (value != null && !value.isEmpty()) ? Double.parseDouble(value) : null;
    }

    @Named("bigDecimalToString")
    public static String bigDecimalToString(BigDecimal value) {
        return value != null ? value.toString() : null;
    }

    @Named("integerToString")
    public static String integerToString(Integer value) {
        return value != null ? value.toString() : null;
    }

    @Named("booleanToString")
    public static String booleanToString(Boolean value) {
        return value != null ? value.toString() : null;
    }

    @Named("longToString")
    public static String longToString(Long value) {
        return value != null ? value.toString() : null;
    }

    @Named("doubleToString")
    public static String doubleToString(Double value) {
        return value != null ? value.toString() : null;
    }
} 