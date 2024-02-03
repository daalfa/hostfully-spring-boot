package com.davi.demo.booking.service.service;

import com.davi.demo.booking.service.exception.BadRequestException;
import com.davi.demo.booking.service.exception.NotFoundException;
import com.davi.demo.booking.service.exception.ValidationException;
import com.davi.demo.booking.service.model.Blocking;
import com.davi.demo.booking.service.model.Booking;
import com.davi.demo.booking.service.repository.BlockingRepository;
import com.davi.demo.booking.service.repository.BookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.davi.demo.booking.service.common.DateUtil.parse;

@Service
public class BlockingService {

    private final BlockingRepository blockingRepository;
    private final BookingRepository bookingRepository;
    private final PropertyService propertyService;


    @Autowired
    public BlockingService(BlockingRepository blockingRepository,
                           BookingRepository bookingRepository,
                           PropertyService propertyService) {
        this.blockingRepository = blockingRepository;
        this.bookingRepository = bookingRepository;
        this.propertyService = propertyService;
    }

    public Blocking getBlockingById(Long id) {
        if(id == null) {
            throw new ValidationException("Blocking Id is required");
        }
        return blockingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blocking id: {0} not found", id));
    }

    public List<Blocking> getAllBlockings() {
        return blockingRepository.findAll();
    }

    /**
     * Create a new Blocking only if Property already exists.
     * Ignore all other Property fields, except id.
     * Cancel Bookings if date overlaps.
     */
    @Transactional
    public void createBlocking(Blocking blocking) {
        var property = propertyService.getPropertyById(blocking.getProperty().getId());
        blocking.setProperty(property);

        validateStartDateBeforeEndDate(blocking);
        validateNoBlockingsWithSameTimeAndProperty(blocking);

        doCancelBookings(blocking);
        blockingRepository.save(blocking);
    }

    @Transactional
    public void updateBlocking(Long id, Blocking updatedBlocking) {
        blockingRepository.findById(id)
                .ifPresentOrElse(blocking -> {
                    var property = propertyService.getPropertyById(updatedBlocking.getProperty().getId());

                    validateStartDateBeforeEndDate(updatedBlocking);
                    validateNoBlockingsWithSameTimeAndProperty(id, updatedBlocking);
                    doCancelBookings(updatedBlocking);

                    blocking.setProperty(property);
                    blocking.setName(updatedBlocking.getName());
                    blocking.setStartDate(updatedBlocking.getStartDate());
                    blocking.setEndDate(updatedBlocking.getEndDate());
                }, () -> {
                    throw new NotFoundException("Blocking id: {0,number,#} not found", id);
                });
    }

    private void validateNoBlockingsWithSameTimeAndProperty(Blocking blocking) {
        validateNoBlockingsWithSameTimeAndProperty(blocking.getId(), blocking);
    }

    /**
     * Check if there are no blocks within the same period
     * In case of updates we ignore the response entity with same id.
     */
    private void validateNoBlockingsWithSameTimeAndProperty(Long id, Blocking blocking) {
    blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
            blocking.getProperty(), blocking.getStartDate(), blocking.getEndDate())
            .stream()
            .map(Blocking::getId)
            .filter(savedId -> !savedId.equals(id))
            .findFirst()
            .ifPresent(existingBlocking -> {
                throw new BadRequestException(
                        "Property is already blocked for this period");
            });
    }

    private void validateStartDateBeforeEndDate(Blocking blocking) {
        var startDate = parse(blocking.getStartDate());
        var endDate = parse(blocking.getEndDate());

        if(startDate.isAfter(endDate) || startDate.equals(endDate)) {
            throw new ValidationException(
                    "Blocking endDate must be after startDate");
        }
    }

    private void doCancelBookings(Blocking blocking) {
        bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                        blocking.getProperty(), blocking.getStartDate(), blocking.getEndDate(), false)
                .forEach(booking -> booking.setIsCanceled(true));
    }

    @Transactional
    public void deleteBlocking(Long id) {
        blockingRepository.findById(id)
                .ifPresentOrElse(blocking -> {
                    blockingRepository.deleteById(id);
                }, () -> {
                    throw new NotFoundException("Blocking id: {0,number,#} not found", id);
                });
    }
}
