package com.davi.demo.booking.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static java.text.MessageFormat.format;

@Getter
public class NotFoundException extends BaseException {

    public NotFoundException(String format, Object... args) {
        super(format(format, args));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
