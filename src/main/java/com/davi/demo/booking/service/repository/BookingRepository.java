package com.davi.demo.booking.service.repository;

import com.davi.demo.booking.service.model.Booking;
import com.davi.demo.booking.service.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT b FROM Booking b
            WHERE b.startAt = :startAt
            AND b.property = :property
            AND b.isCanceled = false
            """)
    List<Booking> findActiveBookingsByBookingTimeAndProperty(
            @Param("startAt") String startAt,
            @Param("property") Property property);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.startAt BETWEEN :startTime AND :endTime
            AND b.property = :property
            """)
    List<Booking> findBookingsByPropertyAndBookingTimeRange(
            @Param("property") Property property,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );
}