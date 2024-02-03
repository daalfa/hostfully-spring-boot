package com.davi.demo.booking.service.it;

import com.davi.demo.booking.service.model.Booking;
import com.davi.demo.booking.service.model.Property;
import com.davi.demo.booking.service.repository.BlockingRepository;
import com.davi.demo.booking.service.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.davi.demo.booking.service.TestData.createBooking;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BlockingRepository blockingRepository;

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        blockingRepository.deleteAll();
    }

    // Happy Path

    @Test
    void shouldLoadBookings() {
        bookingRepository.save(createBooking("test"));

        ResponseEntity<Booking[]> response =
                restTemplate.getForEntity("/api/guest/bookings", Booking[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void shouldLoadSingleBooking() {
        var booking = createBooking("test");
        Long id = bookingRepository.save(booking).getId();

        ResponseEntity<Booking> response =
                restTemplate.getForEntity(
                        "/api/guest/bookings/"+id,
                        Booking.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(booking);
    }

    @Test
    void shouldCreateBooking() {
        var booking = createBooking("test");

        ResponseEntity<Void> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        booking,
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void shouldUpdateBooking() {
        var booking = createBooking("test");
        Long id = bookingRepository.save(booking).getId();

        var updatedBooking = createBooking("new Name");

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/guest/bookings/"+id,
                HttpMethod.PUT,
                new HttpEntity<>(updatedBooking),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(bookingRepository.findById(id))
                .map(Booking::getName).hasValue("new Name");
    }

    @Test
    void shouldDeleteBooking() {
        var booking = createBooking("test");
        Long id = bookingRepository.save(booking).getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/guest/bookings/"+id,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(bookingRepository.findById(id)).isEmpty();
    }

    // Unhappy Path

    @Test
    void givenInvalidPathVariable_whenGetBooking_thenShouldReturn400() {
        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/api/guest/bookings/a",
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("Id must be a number");
    }

    @Test
    void givenNotExistingBooking_whenGetBooking_thenShouldReturn404() {
        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/api/guest/bookings/99",
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .contains("Booking id: 99 not found");
    }

    @Test
    void givenBookingWithInvalidProperty_whenCreateNewBooking_thenShouldNotCreate() {
        var property = new Property();
        property.setId(99L);

        var booking = createBooking("test");
        booking.setProperty(property);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        booking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .contains("Property id: 99 not found");
    }

    @Test
    void givenBookingWithPropertyNoId_whenCreateNewBooking_thenShouldNotCreate() {
        var property = new Property();
        property.setId(null);

        var booking = createBooking("test");
        booking.setProperty(property);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        booking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("Property Id is required");
    }

    //JSR-303
    @Test
    void givenBookingNullProperty_whenCreateNewBooking_thenShouldNotCreate() {
        var booking = createBooking("test");
        booking.setProperty(null);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        booking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("property is mandatory");
    }

    //JSR-303
    @Test
    void givenBookingNoName_whenCreateNewBooking_thenShouldNotCreate() {
        var booking = createBooking("test");
        booking.setName(null);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        booking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("name is mandatory");
    }

    //JSR-303
    @Test
    void givenBookingNameLessThan2Char_whenCreateNewBooking_thenShouldNotCreate() {
        var booking = createBooking("test");
        booking.setName("a");

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        booking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("Name must be between 2 and 50 characters");
    }

    //JSR-303
    @Test
    void givenBookingDescriptionLessThan2Char_whenCreateNewBooking_thenShouldNotCreate() {
        var booking = createBooking("test");
        booking.setDescription("a");

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        booking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("Description must be between 2 and 100 characters");
    }

    //JSR-303
    @Test
    void givenBookingWithoutStartDate_whenCreateNewBooking_thenShouldNotCreate() {
        var booking = createBooking("test");
        booking.setStartDate(null);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        booking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("startDate is mandatory");
    }

    //JSR-303
    @Test
    void givenBookingWithoutEndDate_whenCreateNewBooking_thenShouldNotCreate() {
        var booking = createBooking("test");
        booking.setEndDate(null);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        booking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("endDate is mandatory");
    }

    @Test
    void givenExistingBooking_whenCreateNewBookingSameTime_thenShouldNotCreate() {
        var booking = createBooking("test");
        bookingRepository.save(booking);
        var duplicatedBooking = createBooking("test");

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        duplicatedBooking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("Property is already booked for this period");
    }

    @Test
    void givenABookingWithWrongTimeFormat_whenCreateNewBooking_thenShouldNotCreate() {
        var booking = createBooking("test");
        booking.setStartDate("2024-01-01 1:00 PM");

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        booking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("Invalid date format, correct format is yyyy-MM-dd HH:mm:ss");
    }

    @Test
    void givenABookingWithCanceledTrue_whenCreateNewBooking_thenShouldNotCreate() {
        var booking = createBooking("test");
        booking.setIsCanceled(true);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/guest/bookings",
                        booking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("Cannot create a canceled booking");
    }

    @Test
    void givenABookingWithSameTime_whenUpdateBookingWithSameTime_thenShouldNotUpdate() {
        var booking = createBooking("test");
        var time = booking.getStartDate();
        bookingRepository.save(booking);

        var subject = createBooking("test");
        subject.setStartDate("2024-01-01 09:00:00");

        Long id = bookingRepository.save(subject).getId();
        subject.setStartDate(time);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/guest/bookings/"+id,
                HttpMethod.PUT,
                new HttpEntity<>(subject),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("Property is already booked for this period");
    }

    @Test
    void givenExistingBooking_whenUpdateBookingWithNotExistingPropertyId_thenShouldNotUpdate() {
        var booking = createBooking("test");
        Long id = bookingRepository.save(booking).getId();

        var property = new Property();
        property.setId(9999L);

        var updatedBooking = createBooking("test");
        updatedBooking.setProperty(property);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/guest/bookings/"+id,
                HttpMethod.PUT,
                new HttpEntity<>(updatedBooking),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .contains("Property id: 9999 not found");
    }
}
