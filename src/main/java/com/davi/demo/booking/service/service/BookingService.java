package com.davi.demo.booking.service.service;

import com.davi.demo.booking.service.exception.BadRequestException;
import com.davi.demo.booking.service.exception.NotFoundException;
import com.davi.demo.booking.service.exception.ValidationException;
import com.davi.demo.booking.service.model.Booking;
import com.davi.demo.booking.service.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.davi.demo.booking.service.common.DateUtil.validateMinuteAndSecondIsZero;

@Service
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final PropertyService propertyService;


    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          PropertyService propertyService) {
        this.bookingRepository = bookingRepository;
        this.propertyService = propertyService;
    }

    public Booking getBookingById(Long id) {
        if(id == null) {
            throw new ValidationException("Booking Id is required");
        }
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking id: {0} not found", id));
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Create a new Booking only if Property already exists.
     * StartAt must be exact hour like 01:00:00 or 23:00:00
     * Ignore all other Property fields, except id.
     */
    @Transactional
    public void createBooking(Booking booking) {
        if(booking.getIsCanceled())
            throw new ValidationException("Cannot create a canceled booking");

        validateMinuteAndSecondIsZero(booking.getStartAt());

        var property = propertyService.getPropertyById(booking.getProperty().getId());
        booking.setProperty(property);
        validateBookingsWithSameTimeAndProperty(booking);

        bookingRepository.save(booking);
    }

    /**
     * Update a Booking only if Property already exists.
     * StartAt must be exact hour like 01:00:00 or 23:00:00
     * Ignore all other Property fields, except id.
     */
    @Transactional
    public void updateBooking(Long id, Booking updatedBooking) {
        bookingRepository.findById(id)
                .ifPresentOrElse(booking -> {
                    validateBookingsWithSameTimeAndProperty(id, updatedBooking);
                    booking.setName(updatedBooking.getName());
                    booking.setDescription(updatedBooking.getDescription());
                    booking.setIsCanceled(updatedBooking.getIsCanceled());
                    booking.setStartAt(updatedBooking.getStartAt());
                    updateProperty(booking, updatedBooking);
                }, () -> {
                    throw new NotFoundException("Booking id: {0} not found", id);
                });
    }

    private void validateBookingsWithSameTimeAndProperty(Booking booking) {
        validateBookingsWithSameTimeAndProperty(booking.getId(), booking);
    }

    /**
     * Check if there are no active bookings with same time and property.
     * Active booking has isCanceled = false.
     */
    private void validateBookingsWithSameTimeAndProperty(Long id, Booking booking) {
        bookingRepository.findActiveBookingsByBookingTimeAndProperty(
                        booking.getStartAt(), booking.getProperty())
                .stream()
                .map(Booking::getId)
                .filter(savedId -> !savedId.equals(id))
                .findFirst()
                .ifPresent(existingBooking -> {
                    throw new BadRequestException(
                            "A booking for the same time and property already exist");
                });
    }

    /**
     * Check if Property has changed, if yes then fetch and update.
     */
    private void updateProperty(Booking booking, Booking updatedBooking) {
        var property = Objects.equals(booking.getProperty(), updatedBooking.getProperty())
                ? updatedBooking.getProperty()
                : propertyService.getPropertyById(updatedBooking.getProperty().getId());
        booking.setProperty(property);
    }

    @Transactional
    public void deleteBooking(Long id) {
        bookingRepository.findById(id)
                .ifPresentOrElse(booking -> {
                    bookingRepository.deleteById(id);
                }, () -> {
                    throw new NotFoundException("Booking id: {0,number,#} not found", id);
                });
    }
}
