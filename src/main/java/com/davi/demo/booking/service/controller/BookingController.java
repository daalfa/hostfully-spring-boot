package com.davi.demo.booking.service.controller;

import com.davi.demo.booking.service.exception.BadRequestException;
import com.davi.demo.booking.service.model.Booking;
import com.davi.demo.booking.service.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/guest")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Booking getBookingById(@PathVariable String id) {
        return bookingService.getBookingById(toLong(id));
    }

    @GetMapping("/bookings")
    @ResponseStatus(HttpStatus.OK)
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public void createBooking(@Valid @RequestBody Booking booking) {
        bookingService.createBooking(booking);
    }

    @PutMapping("/bookings/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateBookings(@PathVariable String id, @Valid @RequestBody Booking booking) {
        bookingService.updateBooking(toLong(id), booking);
    }

    @DeleteMapping("/bookings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBookings(@PathVariable String id) {
        bookingService.deleteBooking(toLong(id));
    }

    private long toLong(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Id must be a number");
        }
    }
}
