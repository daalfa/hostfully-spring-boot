package com.davi.demo.booking.service.configuration;

import com.davi.demo.booking.service.exception.BadRequestException;
import com.davi.demo.booking.service.exception.BaseException;
import com.davi.demo.booking.service.common.ErrorResponse;
import com.davi.demo.booking.service.exception.NotFoundException;
import com.davi.demo.booking.service.exception.ValidationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@RestControllerAdvice
public class ResponseExceptionHandler {

    /**
     * Handle all manually throw exceptions
     * Response code depends on the exception
     */
    @ExceptionHandler(value = {NotFoundException.class, BadRequestException.class, ValidationException.class})
    public ResponseEntity<ErrorResponse> handleCustomException(BaseException e) {
        var errorResponse = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(errorResponse, e.getStatus());
    }

    /**
     * Handle JSR-303 bean validation
     * Response code is BadRequest 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = Optional.ofNullable(ex.getBindingResult().getFieldError())
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse(null);
        var errorResponse = new ErrorResponse(message);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle uncaught exceptions to return Error payload with Exception Message
     * Some ValidationException may be wrapped in JPAException.
     * If this is the case, the response code is BadRequest 400
     * For all other unhandled exceptions, the response code is 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralExceptions(Exception e) {
        return Optional.ofNullable(e.getCause())
                .map(Throwable::getCause)
                .filter(ValidationException.class::isInstance)
                .map(ValidationException.class::cast)
                .map(this::handleCustomException)
                .orElseGet(() -> {
                    var errorResponse = new ErrorResponse(e.getMessage());
                    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
}