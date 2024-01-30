package com.davi.demo.booking.service.common;

import com.davi.demo.booking.service.exception.ValidationException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DateTimeException;
import java.time.LocalDateTime;

import static com.davi.demo.booking.service.common.DateUtil.DATE_FORMAT;

@Converter
public class LocalDateTimeConverter implements AttributeConverter<String, LocalDateTime> {

    @Override
    public LocalDateTime convertToDatabaseColumn(String attribute) {
        return DateUtil.parse(attribute);
    }

    @Override
    public String convertToEntityAttribute(LocalDateTime column) {
        return DateUtil.format(column);
    }
}
