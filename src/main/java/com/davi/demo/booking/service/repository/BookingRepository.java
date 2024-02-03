package com.davi.demo.booking.service.repository;

import com.davi.demo.booking.service.model.Booking;
import com.davi.demo.booking.service.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT b FROM Booking b
            WHERE b.property = :property
            AND b.isCanceled = :isCanceled
            AND ((b.startDate < :endDate AND b.endDate > :startDate)
                OR
                (b.startDate = :startDate AND b.endDate = :endDate))
            """)
    List<Booking> findBookingsByPropertyAndBookingTimeRangeAndStatus(
            @Param("property") Property property,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("isCanceled") Boolean isCanceled
    );
}