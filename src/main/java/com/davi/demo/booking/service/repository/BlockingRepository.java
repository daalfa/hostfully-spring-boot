package com.davi.demo.booking.service.repository;

import com.davi.demo.booking.service.model.Blocking;
import com.davi.demo.booking.service.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockingRepository extends JpaRepository<Blocking, Long> {

    @Query("""
            SELECT b FROM Blocking b
            WHERE b.blockingTime BETWEEN :startTime AND :endTime
            AND b.property = :property
            """)
    List<Blocking> findBlockingsByPropertyAndBookingTimeRange(
            @Param("property") Property property,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);
}