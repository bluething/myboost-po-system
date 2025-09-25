package io.github.bluething.myboostposystem.common;

import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for timezone operations
 * Uses UTC for storage and configurable timezone for display
 * Timezone is configurable via application properties
 */
@Component
public class TimezoneUtil {

    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter API_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Injected from properties
    private static ZoneId APP_ZONE;

    /**
     * Sets the application timezone from Spring properties
     * @param timezone timezone string from application.yml
     */
    @Value("${app.timezone:Asia/Jakarta}")
    public void setAppTimezone(String timezone) {
        APP_ZONE = ZoneId.of(timezone);
    }

    /**
     * Gets current instant (UTC)
     * @return current Instant in UTC
     */
    public static Instant now() {
        return Instant.now();
    }

    /**
     * Gets current datetime in application timezone
     * @return ZonedDateTime in application timezone
     */
    public static ZonedDateTime nowInAppZone() {
        return ZonedDateTime.now(getAppZone());
    }

    /**
     * Gets the configured application timezone
     * @return configured ZoneId
     */
    public static ZoneId getAppZone() {
        return APP_ZONE != null ? APP_ZONE : ZoneId.of("Asia/Jakarta"); // fallback
    }

    /**
     * Converts Instant to ZonedDateTime in application timezone
     * @param instant UTC instant
     * @return ZonedDateTime in application timezone
     */
    public static ZonedDateTime toAppZone(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(getAppZone());
    }

    /**
     * Converts Instant to LocalDateTime in application timezone
     * Useful for display purposes
     * @param instant UTC instant
     * @return LocalDateTime in application timezone
     */
    public static LocalDateTime toAppLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(getAppZone()).toLocalDateTime();
    }

    /**
     * Converts LocalDateTime from application timezone to Instant (UTC)
     * @param localDateTime datetime in application timezone
     * @return Instant in UTC
     */
    public static Instant fromAppZone(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.atZone(getAppZone()).toInstant();
    }

    /**
     * Formats Instant for display in application timezone
     * @param instant UTC instant
     * @return formatted string in application timezone
     */
    public static String formatForDisplay(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(getAppZone()).format(DISPLAY_FORMATTER);
    }

    /**
     * Formats Instant for API response
     * @param instant UTC instant
     * @return ISO formatted string
     */
    public static String formatForApi(Instant instant) {
        if (instant == null) return null;
        return instant.toString(); // ISO-8601 format with 'Z' suffix
    }

    /**
     * Formats Instant for API response in local timezone (without timezone info)
     * Useful when client expects local datetime format
     * @param instant UTC instant
     * @return formatted LocalDateTime string in application timezone
     */
    public static String formatForApiLocal(Instant instant) {
        if (instant == null) return null;
        return toAppLocalDateTime(instant).format(API_FORMATTER);
    }

    /**
     * Parses ISO instant string to Instant
     * @param instantString ISO formatted instant string (with Z or timezone offset)
     * @return Instant in UTC
     */
    public static Instant parseFromApi(String instantString) {
        if (instantString == null || instantString.trim().isEmpty()) return null;

        // Handle both ISO instant format and local datetime format
        if (instantString.endsWith("Z") || instantString.contains("+") || instantString.matches(".*[+-]\\d{2}:\\d{2}$")) {
            return Instant.parse(instantString);
        } else {
            // Assume local datetime in application timezone
            LocalDateTime localDateTime = LocalDateTime.parse(instantString, API_FORMATTER);
            return fromAppZone(localDateTime);
        }
    }

    /**
     * Gets timezone offset string for application timezone
     * @return offset string (e.g., "+07:00")
     */
    public static String getAppZoneOffset() {
        return ZonedDateTime.now(getAppZone()).getOffset().toString();
    }

    /**
     * Gets timezone display name
     * @return timezone display name
     */
    public static String getAppZoneDisplayName() {
        return getAppZone().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
    }

    /**
     * Converts between different timezones
     * @param instant UTC instant
     * @param targetZone target timezone
     * @return ZonedDateTime in target timezone
     */
    public static ZonedDateTime toZone(Instant instant, ZoneId targetZone) {
        if (instant == null) return null;
        return instant.atZone(targetZone);
    }

    /**
     * Checks if given instant is today in application timezone
     * @param instant UTC instant to check
     * @return true if the instant is today in configured timezone
     */
    public static boolean isToday(Instant instant) {
        if (instant == null) return false;
        LocalDate instantDate = instant.atZone(getAppZone()).toLocalDate();
        LocalDate today = LocalDate.now(getAppZone());
        return instantDate.equals(today);
    }

    /**
     * Gets start of day for given date in application timezone
     * @param date the date
     * @return Instant representing start of day in UTC
     */
    public static Instant startOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay(getAppZone()).toInstant();
    }

    /**
     * Gets end of day for given date in application timezone
     * @param date the date
     * @return Instant representing end of day in UTC
     */
    public static Instant endOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atTime(LocalTime.MAX).atZone(getAppZone()).toInstant();
    }

    /**
     * Gets current date in application timezone
     * @return current LocalDate in configured timezone
     */
    public static LocalDate currentDate() {
        return LocalDate.now(getAppZone());
    }

    /**
     * Gets current time in application timezone
     * @return current LocalTime in configured timezone
     */
    public static LocalTime currentTime() {
        return LocalTime.now(getAppZone());
    }
}
