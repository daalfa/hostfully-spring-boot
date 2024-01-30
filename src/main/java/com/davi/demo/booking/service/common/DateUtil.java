package com.davi.demo.booking.service.common;

import com.davi.demo.booking.service.exception.ValidationException;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

    public static LocalDateTime parse(String date) {
        try {
            return LocalDateTime.parse(date.trim(), formatter);
        } catch (DateTimeException e) {
            throw new ValidationException("Invalid date format, correct format is {0}", DATE_FORMAT);
        }
    }

    public static String format(LocalDateTime date) {
        try {
            return date.format(formatter);
        } catch (DateTimeException e) {
            return null;
        }
    }

    public static void validateMinuteAndSecondIsZero(String date) {
        var dateTime = parse(date);
        if(dateTime.getMinute() > 0 || dateTime.getSecond() > 0)
            throw new ValidationException("Minutes and seconds must be 0");
    }

    public static String getSameDayStartTime(String date) {
        var dateTime = parse(date).withHour(0).withMinute(0).withSecond(0);
        return format(dateTime);
    }

    public static String getSameDayEndTime(String date) {
        var dateTime = parse(date).withHour(23).withMinute(59).withSecond(59);
        return format(dateTime);
    }
}
