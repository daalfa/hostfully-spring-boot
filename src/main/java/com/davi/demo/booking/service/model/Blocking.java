package com.davi.demo.booking.service.model;

import com.davi.demo.booking.service.common.LocalDateTimeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blocking")
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Blocking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @NotBlank(message = "startDate is mandatory")
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "start_date", nullable = false)
    private String startDate;

    @NotBlank(message = "endDate is mandatory")
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "end_date", nullable = false)
    private String endDate;

    @NotNull(message = "property is mandatory")
    @OneToOne
    @JoinColumn(name = "property_id")
    private Property property;
}
