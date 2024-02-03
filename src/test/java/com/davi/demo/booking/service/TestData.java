package com.davi.demo.booking.service;

import com.davi.demo.booking.service.model.Blocking;
import com.davi.demo.booking.service.model.Booking;
import com.davi.demo.booking.service.model.Property;

public class TestData {

    public static Booking createBooking(String name) {
        var property = new Property();
        property.setId(1L);

        var booking = new Booking();
        booking.setName(name);
        booking.setDescription("description");
        booking.setIsCanceled(false);
        booking.setStartDate("2024-01-01 01:00:00");
        booking.setEndDate("2024-01-01 02:00:00");
        booking.setProperty(property);
        return booking;
    }

    public static Blocking createBlocking(String name) {
        var property = new Property();
        property.setId(1L);

        var blocking = new Blocking();
        blocking.setName(name);
        blocking.setStartDate("2024-01-01 00:00:00");
        blocking.setEndDate("2024-01-02 00:00:00");
        blocking.setProperty(property);
        return blocking;
    }
}
