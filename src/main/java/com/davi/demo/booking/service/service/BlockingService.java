package com.davi.demo.booking.service.service;

import com.davi.demo.booking.service.exception.BadRequestException;
import com.davi.demo.booking.service.exception.NotFoundException;
import com.davi.demo.booking.service.exception.ValidationException;
import com.davi.demo.booking.service.model.Blocking;
import com.davi.demo.booking.service.repository.BlockingRepository;
import com.davi.demo.booking.service.repository.BookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.davi.demo.booking.service.common.DateUtil.getSameDayEndTime;
import static com.davi.demo.booking.service.common.DateUtil.getSameDayStartTime;

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
     * Cancel Bookings if date is the same day.
     * The time portion of blockingTime hour, minute and second is ignored,
     * since blocking is daily, we validate if no block with same day exist.
     */
    @Transactional
    public void createBlocking(Blocking blocking) {
        var property = propertyService.getPropertyById(blocking.getProperty().getId());
        blocking.setProperty(property);

        var start = getSameDayStartTime(blocking.getBlockingTime());
        var end = getSameDayEndTime(blocking.getBlockingTime());

        blockingRepository.findBlockingsByPropertyAndBookingTimeRange(property,
                start, end).stream().findFirst()
                .ifPresent(existing -> {
                    throw new BadRequestException(
                            "A blocking for the same day and property already exist");
                });

        bookingRepository.findBookingsByPropertyAndBookingTimeRange(property,
                        start, end)
                .forEach(booking -> booking.setIsCanceled(true));

        blockingRepository.save(blocking);
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
