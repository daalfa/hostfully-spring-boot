package com.davi.demo.booking.service.model;

import com.davi.demo.booking.service.common.LocalDateTimeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking")
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "name is mandatory")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Size(min = 2, max = 100, message = "Description must be between 2 and 100 characters")
    private String description;

    @NotBlank(message = "startDate is mandatory")
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "start_date", nullable = false)
    private String startDate;

    @NotBlank(message = "endDate is mandatory")
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "end_date", nullable = false)
    private String endDate;

    @Column(name = "is_canceled")
    private Boolean isCanceled = false;

    @NotNull(message = "property is mandatory")
    @OneToOne
    @JoinColumn(name = "property_id")
    private Property property;
}
