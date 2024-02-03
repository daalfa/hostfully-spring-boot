package com.davi.demo.booking.service.service;

import com.davi.demo.booking.service.exception.BadRequestException;
import com.davi.demo.booking.service.exception.NotFoundException;
import com.davi.demo.booking.service.exception.ValidationException;
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
import static com.davi.demo.booking.service.TestData.createBooking;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BlockingRepository blockingRepository;

    @Mock
    private PropertyService propertyService;

    @InjectMocks
    private BookingService bookingService;

    @Captor
    private ArgumentCaptor<Booking> saveBookingCaptor;

    @Test
    public void givenValidId_whenGetBookingById_thenReturnBooking() {
        Long id = 1L;
        var booking = createBooking("test");
        booking.setId(id);

        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(booking));

        var resultBooking = bookingService.getBookingById(id);

        assertThat(resultBooking).isEqualTo(booking);
    }

    @Test
    public void givenNullId_whenGetBookingById_thenThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.getBookingById(null);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Booking Id is required");
    }

    @Test
    public void givenNotExistingId_whenGetBookingById_thenThrowNotFoundException() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.getBookingById(99L);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Booking id: 99 not found");
    }

    // DELETE TESTS

    @Test
    public void givenValidId_whenDeleteBooking_thenDelete() {
        Long id = 1L;
        var booking = createBooking("test");

        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(booking));

        bookingService.deleteBooking(id);

        verify(bookingRepository).deleteById(id);
    }

    @Test
    public void givenNotExistingId_whenDeleteBooking_thenThrowNotFoundException() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.deleteBooking(99L);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Booking id: 99 not found");
    }

    // CREATE TESTS

    @Test
    public void givenValidBooking_whenCreateBooking_thenCreate() {
        var booking = createBooking("test");

        when(propertyService.getPropertyById(booking.getProperty().getId()))
                .thenReturn(booking.getProperty());

        when(bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                booking.getProperty(), booking.getStartDate(), booking.getEndDate(), false))
                .thenReturn(emptyList());

        when(blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
                booking.getProperty(), booking.getStartDate(), booking.getEndDate()))
                .thenReturn(emptyList());

        when(bookingRepository.save(saveBookingCaptor.capture()))
                .thenReturn(booking);

        bookingService.createBooking(booking);

        assertThat(saveBookingCaptor.getValue()).isEqualTo(booking);
    }

    @Test
    public void givenCanceledBooking_whenCreateBooking_thenThrowValidationException() {
        var booking = createBooking("test");
        booking.setIsCanceled(true);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(booking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Cannot create a canceled booking");
    }

    @Test
    public void givenExistBookingWithSameTimeAndProperty_whenCreateBooking_thenThrowBadRequestException() {
        var booking = createBooking("test");
        var existingBooking = createBooking("existing booking");
        existingBooking.setId(2L);

        when(propertyService.getPropertyById(booking.getProperty().getId()))
                .thenReturn(booking.getProperty());

        when(bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                booking.getProperty(), booking.getStartDate(), booking.getEndDate(), booking.getIsCanceled()))
                .thenReturn(List.of(existingBooking));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            bookingService.createBooking(booking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Property is already booked for this period");
    }

    @Test
    public void givenExistBlockWithSameTimeAndProperty_whenCreateBooking_thenThrowBadRequestException() {
        var booking = createBooking("test");
        var blocking = createBlocking("block");

        when(propertyService.getPropertyById(booking.getProperty().getId()))
                .thenReturn(booking.getProperty());

        when(bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                booking.getProperty(), booking.getStartDate(), booking.getEndDate(), false))
                .thenReturn(emptyList());

        when(blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
                booking.getProperty(), booking.getStartDate(), booking.getEndDate()))
                .thenReturn(List.of(blocking));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            bookingService.createBooking(booking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Property is blocked for this period");
    }

    @Test
    public void givenInvalidStartEndDate_whenCreateBooking_thenThrowValidationException() {
        var booking = createBooking("test");
        booking.setStartDate("2024-01-02 12:00:00");
        booking.setEndDate("2024-01-02 01:00:00");

        when(propertyService.getPropertyById(booking.getProperty().getId()))
                .thenReturn(booking.getProperty());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(booking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Booking endDate must be after startDate");
    }

    // UPDATE TESTS

    @Test
    public void givenValidId_whenUpdateBooking_thenUpdate() {
        Long id = 1L;
        var updatedBooking = createBooking("test");

        var existingBooking = mock(Booking.class);

        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(existingBooking));

        when(bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                updatedBooking.getProperty(), updatedBooking.getStartDate(),
                updatedBooking.getEndDate(), false))
                .thenReturn(emptyList());

        when(propertyService.getPropertyById(updatedBooking.getProperty().getId()))
                .thenReturn(updatedBooking.getProperty());

        when(blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
                updatedBooking.getProperty(), updatedBooking.getStartDate(), updatedBooking.getEndDate()))
                .thenReturn(emptyList());

        bookingService.updateBooking(id, updatedBooking);

        verify(existingBooking).setName(updatedBooking.getName());
        verify(existingBooking).setDescription(updatedBooking.getDescription());
        verify(existingBooking).setIsCanceled(updatedBooking.getIsCanceled());
        verify(existingBooking).setStartDate(updatedBooking.getStartDate());
        verify(existingBooking).setEndDate(updatedBooking.getEndDate());
        verify(existingBooking).setProperty(updatedBooking.getProperty());
    }

    @Test
    public void givenInvalidId_whenUpdateBooking_thenThrowNotFoundException() {
        Long id = 1L;
        var booking = createBooking("test");

        when(bookingRepository.findById(id))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookingService.updateBooking(id, booking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Booking id: 1 not found");
    }

    @Test
    public void givenExistingActiveBookingWithSameTimeAndProperty_whenUpdateBooking_thenThrowBadRequestException() {
        Long id = 1L;
        var booking = createBooking("test");

        var existingBooking = createBooking("existing booking");
        existingBooking.setId(2L);

        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                booking.getProperty(), booking.getStartDate(), booking.getEndDate(), false))
                .thenReturn(List.of(existingBooking));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            bookingService.updateBooking(id, booking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo(
                "Property is already booked for this period");
    }

    @Test
    public void givenExistingBlockWithSameTimeAndProperty_whenUpdateBooking_thenThrowBadRequestException() {
        Long id = 1L;
        var booking = createBooking("test");

        var blocking = createBlocking("block");

        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.findBookingsByPropertyAndBookingTimeRangeAndStatus(
                booking.getProperty(), booking.getStartDate(), booking.getEndDate(), false))
                .thenReturn(emptyList());

        when(blockingRepository.findBlockingsByPropertyAndBlockingTimeRange(
                booking.getProperty(), booking.getStartDate(), booking.getEndDate()))
                .thenReturn(List.of(blocking));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            bookingService.updateBooking(id, booking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo(
                "Property is blocked for this period");
    }

    @Test
    public void givenInvalidStartEndDate_whenUpdateBooking_thenThrowValidationException() {
        Long id = 1L;
        var booking = createBooking("test");
        booking.setStartDate("2024-01-02 12:00:00");
        booking.setEndDate("2024-01-02 01:00:00");

        when(bookingRepository.findById(id))
                .thenReturn(Optional.of(booking));

        when(propertyService.getPropertyById(booking.getProperty().getId()))
                .thenReturn(booking.getProperty());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.updateBooking(id, booking);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Booking endDate must be after startDate");
    }
}