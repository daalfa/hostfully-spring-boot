package com.davi.demo.booking.service.repository;

import com.davi.demo.booking.service.model.Booking;
import com.davi.demo.booking.service.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
}