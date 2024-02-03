package com.davi.demo.booking.service.service;

import com.davi.demo.booking.service.exception.BadRequestException;
import com.davi.demo.booking.service.exception.NotFoundException;
import com.davi.demo.booking.service.exception.ValidationException;
import com.davi.demo.booking.service.model.Blocking;
import com.davi.demo.booking.service.model.Booking;
import com.davi.demo.booking.service.repository.BlockingRepository;
import com.davi.demo.booking.service.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static com.davi.demo.booking.service.TestData.createBlocking;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockingServiceTest {

    @Mock
    private BlockingRepository blockingRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PropertyService propertyService;

    @InjectMocks
    private BlockingService blockingService;

    @Captor
    private ArgumentCaptor<Blocking> saveBlockingCaptor;

    @Test
    public void givenValidId_whenGetBlockingById_thenReturnBlocking() {
        Long id = 1L;
        var blocking = createBlocking("test");
        blocking.setId(id);

        when(blockingRepository.findById(id))
                .thenReturn(Optional.of(blocking));

        Blocking resultBlocking = blockingService.getBlockingById(id);

        assertThat(resultBlocking).isEqualTo(blocking);
    }

    @Test
    public void givenNullId_whenGetBlockingById_thenThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            blockingService.getBlockingById(null);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Blocking Id is required");
    }

    @Test
    public void givenNotExistingId_whenGetBlockingById_thenThrowNotFoundException() {
        when(blockingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            blockingService.getBlockingById(99L);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Blocking id: 99 not found");
    }

    // DELETE TESTS

    @Test
    public void givenValidId_whenDeleteBlocking_thenDelete() {
        Long id = 1L;
        var blocking = createBlocking("test");

        when(blockingRepository.findById(id))
                .thenReturn(Optional.of(blocking));

        blockingService.deleteBlocking(id);

        verify(blockingRepository).deleteById(id);
    }

    @Test
    public void givenNotExistingId_whenDeleteBlocking_thenThrowNotFoundException() {
        when(blockingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            blockingService.deleteBlocking(99L);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Blocking id: 99 not found");
    }

    // CREATE TESTS

    @Test
    public void givenValidBlocking_whenCreateBlocking_thenCreate() {
        var blocking = createBlocking("test");

        when(propertyService.getPropertyById(blocking.getProperty().getId()))
                .thenReturn(blocking.getProperty());

        when(blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
                eq(blocking.getProperty()), anyString(), anyString()))
                .thenReturn(emptyList());

        when(bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                eq(blocking.getProperty()), anyString(), anyString(), anyBoolean()))
                .thenReturn(emptyList());

        when(blockingRepository.save(saveBlockingCaptor.capture()))
                .thenReturn(blocking);

        blockingService.createBlocking(blocking);

        assertThat(saveBlockingCaptor.getValue()).isEqualTo(blocking);
    }

    @Test
    public void givenExistingBookingsWithinPeriod_whenCreateBlocking_thenCreateAndCancelBookings() {
        var blocking = createBlocking("test");

        Booking booking1 = mock(Booking.class);
        Booking booking2 = mock(Booking.class);

        when(propertyService.getPropertyById(blocking.getProperty().getId()))
                .thenReturn(blocking.getProperty());

        when(blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
                eq(blocking.getProperty()), anyString(), anyString()))
                .thenReturn(emptyList());

        when(bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                eq(blocking.getProperty()), anyString(), anyString(), anyBoolean()))
                .thenReturn(List.of(booking1, booking2));

        when(blockingRepository.save(saveBlockingCaptor.capture()))
                .thenReturn(blocking);

        blockingService.createBlocking(blocking);

        assertThat(saveBlockingCaptor.getValue()).isEqualTo(blocking);

        verify(booking1).setIsCanceled(true);
        verify(booking2).setIsCanceled(true);
    }

    @Test
    public void givenExistBlockingWithSameTimeAndProperty_whenCreateBlocking_thenThrowBadRequestException() {
        Long id = 2L;
        var existingBlocking = createBlocking("existing block");
        existingBlocking.setId(id);

        var blocking = createBlocking("test");

        when(propertyService.getPropertyById(blocking.getProperty().getId()))
                .thenReturn(blocking.getProperty());

        when(blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
                eq(blocking.getProperty()), anyString(), anyString()))
                .thenReturn(List.of(existingBlocking));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            blockingService.createBlocking(blocking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Property is already blocked for this period");
    }

    @Test
    public void givenInvalidStartEndDate_whenCreateBlocking_thenThrowValidationException() {
        var blocking = createBlocking("test");
        blocking.setStartDate("2024-01-02 12:00:00");
        blocking.setEndDate("2024-01-02 01:00:00");

        when(propertyService.getPropertyById(blocking.getProperty().getId()))
                .thenReturn(blocking.getProperty());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            blockingService.createBlocking(blocking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Blocking endDate must be after startDate");
    }

    // UPDATE TESTS

    @Test
    public void givenValidId_whenUpdateBlocking_thenUpdate() {
        Long id = 1L;
        var updatedBlocking = createBlocking("test");
        var existingBlocking = mock(Blocking.class);

        when(blockingRepository.findById(id))
                .thenReturn(Optional.of(existingBlocking));

        when(propertyService.getPropertyById(updatedBlocking.getProperty().getId()))
                .thenReturn(updatedBlocking.getProperty());

        when(blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
                eq(updatedBlocking.getProperty()), anyString(), anyString()))
                .thenReturn(emptyList());

        when(bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                eq(updatedBlocking.getProperty()), anyString(), anyString(), anyBoolean()))
                .thenReturn(emptyList());

        blockingService.updateBlocking(id, updatedBlocking);

        verify(existingBlocking).setName(updatedBlocking.getName());
        verify(existingBlocking).setStartDate(updatedBlocking.getStartDate());
        verify(existingBlocking).setEndDate(updatedBlocking.getEndDate());
        verify(existingBlocking).setProperty(updatedBlocking.getProperty());
    }

    @Test
    public void givenExistingBookingsWithinPeriod_whenUpdateBlocking_thenUpdateAndCancelBookings() {
        Long id = 1L;
        var updatedBlocking = createBlocking("test");
        var existingBlocking = mock(Blocking.class);
        var booking1 = mock(Booking.class);
        var booking2 = mock(Booking.class);

        when(blockingRepository.findById(id))
                .thenReturn(Optional.of(existingBlocking));

        when(propertyService.getPropertyById(updatedBlocking.getProperty().getId()))
                .thenReturn(updatedBlocking.getProperty());

        when(blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
                eq(updatedBlocking.getProperty()), anyString(), anyString()))
                .thenReturn(emptyList());

        when(bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                eq(updatedBlocking.getProperty()), anyString(), anyString(), anyBoolean()))
                .thenReturn(List.of(booking1, booking2));

        blockingService.updateBlocking(id, updatedBlocking);

        verify(booking1).setIsCanceled(true);
        verify(booking2).setIsCanceled(true);
    }

    @Test
    public void givenExistBlockingWithSameTimeAndProperty_whenUpdateBlocking_thenThrowBadRequestException() {
        Long id = 1L;

        var existingBlocking = createBlocking("existing block");
        existingBlocking.setId(2L);

        var blocking = createBlocking("test");

        when(blockingRepository.findById(id))
                .thenReturn(Optional.of(blocking));

        when(propertyService.getPropertyById(blocking.getProperty().getId()))
                .thenReturn(blocking.getProperty());

        when(blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
                eq(blocking.getProperty()), anyString(), anyString()))
                .thenReturn(List.of(existingBlocking));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            blockingService.updateBlocking(id, blocking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Property is already blocked for this period");
    }

    @Test
    public void givenInvalidStartEndDate_whenUpdateBlocking_thenThrowValidationException() {
        Long id = 1L;
        var blocking = createBlocking("test");
        blocking.setStartDate("2024-01-02 12:00:00");
        blocking.setEndDate("2024-01-02 01:00:00");

        when(blockingRepository.findById(id))
                .thenReturn(Optional.of(blocking));

        when(propertyService.getPropertyById(blocking.getProperty().getId()))
                .thenReturn(blocking.getProperty());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            blockingService.updateBlocking(id, blocking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Blocking endDate must be after startDate");
    }
}