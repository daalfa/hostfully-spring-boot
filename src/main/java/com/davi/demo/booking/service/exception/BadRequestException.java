package com.davi.demo.booking.service.exception;

import org.springframework.http.HttpStatus;

import static java.text.MessageFormat.format;

public class BadRequestException extends BaseException {

    public BadRequestException(String format, Object... args) {
        super(format(format, args));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
