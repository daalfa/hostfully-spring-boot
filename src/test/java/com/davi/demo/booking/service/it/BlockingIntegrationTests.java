package com.davi.demo.booking.service.it;

import com.davi.demo.booking.service.model.Blocking;
import com.davi.demo.booking.service.model.Booking;
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

import static com.davi.demo.booking.service.TestData.createBlocking;
import static com.davi.demo.booking.service.TestData.createBooking;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BlockingIntegrationTests {

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
    void shouldLoadBlockings() {

        ResponseEntity<Blocking[]> response =
                restTemplate.getForEntity("/api/host/blockings", Blocking[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldLoadSingleBlocking() {
        var blocking = createBlocking("test");
        Long id = blockingRepository.save(blocking).getId();

        ResponseEntity<Blocking> response =
                restTemplate.getForEntity("/api/host/blockings/"+id, Blocking.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(blocking);
    }

    @Test
    void shouldCreateBlocking() {
        var blocking = createBlocking("test");

        ResponseEntity<Void> response =
                restTemplate.postForEntity(
                        "/api/host/blockings",
                        blocking,
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void shouldCreateBlockingAndCancelBookingsWithinSamePeriod() {
        //2024-01-01 01:00:00
        var booking = createBooking("testBooking");
        assertThat(booking.getIsCanceled()).isFalse();
        Long bookingId = bookingRepository.save(booking).getId();

        var blocking = createBlocking("testBlocking");

        ResponseEntity<Void> response =
                restTemplate.postForEntity(
                        "/api/host/blockings",
                        blocking,
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(bookingRepository.findById(bookingId))
                .map(Booking::getIsCanceled)
                .hasValue(true);

    }

    @Test
    void shouldUpdateBlocking() {
        var blocking = createBlocking("block");
        Long id = blockingRepository.save(blocking).getId();

        var updatedBlocking = createBlocking("updated block");

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/host/blockings/"+id,
                HttpMethod.PUT,
                new HttpEntity<>(updatedBlocking),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(blockingRepository.findById(id))
                .map(Blocking::getName).hasValue("updated block");
    }

    @Test
    void shouldDeleteBlocking() {
        var blocking = createBlocking("test");
        Long id = blockingRepository.save(blocking).getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/host/blockings/"+id,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(blockingRepository.findById(id)).isEmpty();
    }

    // Unhappy Path

    @Test
    void givenExistingBlocking_whenCreateNewBlockingWithSameTime_thenShouldNotCreate() {
        //2024-01-01 00:00:00
        var blocking = createBlocking("existing block");
        blockingRepository.save(blocking);

        var sameDayBlocking = createBlocking("test");

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/host/blockings",
                        sameDayBlocking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("Property is already blocked for this period");
    }

    //JSR-303
    @Test
    void givenBlockingWithNoProperty_whenCreateNewBlocking_thenShouldNotCreate() {
        var blocking = createBlocking("test");
        blocking.setProperty(null);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/host/blockings",
                        blocking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("property is mandatory");
    }

    @Test
    void givenBlockingWithPropertyIdNotExist_whenCreateNewBlocking_thenShouldNotCreate() {
        var blocking = createBlocking("test");
        blocking.getProperty().setId(99L);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/host/blockings",
                        blocking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .contains("Property id: 99 not found");
    }

    //JSR-303
    @Test
    void givenBlockingWithNoStartDate_whenCreateNewBlocking_thenShouldNotCreate() {
        var blocking = createBlocking("test");
        blocking.setStartDate(null);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/host/blockings",
                        blocking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("startDate is mandatory");
    }

    //JSR-303
    @Test
    void givenBlockingWithNoEndDate_whenCreateNewBlocking_thenShouldNotCreate() {
        var blocking = createBlocking("test");
        blocking.setEndDate(null);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/host/blockings",
                        blocking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("endDate is mandatory");
    }

    @Test
    void givenBlockingWithInvalidFormatBlockingTime_whenCreateNewBlocking_thenShouldNotCreate() {
        var blocking = createBlocking("test");
        blocking.setStartDate("2024-01-01 12:00:00.000");

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/host/blockings",
                        blocking,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("Invalid date format, correct format is yyyy-MM-dd HH:mm:ss");
    }
}
