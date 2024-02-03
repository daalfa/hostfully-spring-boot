package com.davi.demo.booking.service.service;

import com.davi.demo.booking.service.exception.BadRequestException;
import com.davi.demo.booking.service.exception.NotFoundException;
import com.davi.demo.booking.service.exception.ValidationException;
import com.davi.demo.booking.service.model.Booking;
import com.davi.demo.booking.service.repository.BlockingRepository;
import com.davi.demo.booking.service.repository.BookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.davi.demo.booking.service.common.DateUtil.parse;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BlockingRepository blockingRepository;

    private final PropertyService propertyService;


    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          BlockingRepository blockingRepository,
                          PropertyService propertyService) {
        this.bookingRepository = bookingRepository;
        this.blockingRepository = blockingRepository;
        this.propertyService = propertyService;
    }

    public Booking getBookingById(Long id) {
        if(id == null) {
            throw new ValidationException("Booking Id is required");
        }
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking id: {0,number,#} not found", id));
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Create a new Booking only if Property already exists and there is no Block.
     * Ignore all other Property fields, except id.
     */
    @Transactional
    public void createBooking(Booking booking) {
        if(booking.getIsCanceled())
            throw new ValidationException("Cannot create a canceled booking");

        var property = propertyService.getPropertyById(booking.getProperty().getId());
        booking.setProperty(property);

        validateStartAndEndDate(booking);
        validateNoBookingsWithSameTimeAndProperty(booking);
        validateNoBlockingsWithSameTimeAndProperty(booking);

        bookingRepository.save(booking);
    }

    /**
     * Update a Booking only if Property already exists and there is no block.
     * Ignore all other Property fields, except id.
     */
    @Transactional
    public void updateBooking(Long id, Booking updatedBooking) {
        bookingRepository.findById(id)
                .ifPresentOrElse(booking -> {
                    var property = propertyService.getPropertyById(updatedBooking.getProperty().getId());

                    validateStartAndEndDate(updatedBooking);
                    validateNoBookingsWithSameTimeAndProperty(id, updatedBooking);
                    validateNoBlockingsWithSameTimeAndProperty(updatedBooking);

                    booking.setProperty(property);
                    booking.setName(updatedBooking.getName());
                    booking.setDescription(updatedBooking.getDescription());
                    booking.setIsCanceled(updatedBooking.getIsCanceled());
                    booking.setStartDate(updatedBooking.getStartDate());
                    booking.setEndDate(updatedBooking.getEndDate());
                }, () -> {
                    throw new NotFoundException("Booking id: {0,number,#} not found", id);
                });
    }

    private void validateNoBookingsWithSameTimeAndProperty(Booking booking) {
        validateNoBookingsWithSameTimeAndProperty(booking.getId(), booking);
    }

    /**
     * Check if there are no active bookings with same time and property.
     * Active booking has isCanceled = false.
     * In case of updates we ignore the response entity with same id.
     */
    private void validateNoBookingsWithSameTimeAndProperty(Long id, Booking booking) {
        bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                        booking.getProperty(), booking.getStartDate(), booking.getEndDate(), false)
                .stream()
                .map(Booking::getId)
                .filter(savedId -> !savedId.equals(id))
                .findFirst()
                .ifPresent(existingBooking -> {
                    throw new BadRequestException(
                            "Property is already booked for this period");
                });
    }

    /**
     * Check if there are no blocks with same time and property.
     */
    private void validateNoBlockingsWithSameTimeAndProperty(Booking booking) {
        if(booking.getIsCanceled()) {
            return;
        }
        blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
                        booking.getProperty(), booking.getStartDate(), booking.getEndDate())
                .stream()
                .findFirst()
                .ifPresent(existingBlocking -> {
                    throw new BadRequestException("Property is blocked for this period");
                });
    }

    private void validateStartAndEndDate(Booking booking) {
        var startDate = parse(booking.getStartDate());
        var endDate = parse(booking.getEndDate());

        if(startDate.isAfter(endDate) || startDate.equals(endDate)) {
            throw new ValidationException(
                    "Booking endDate must be after startDate");
        }
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
