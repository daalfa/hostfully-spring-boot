package com.davi.demo.booking.service.service;

import com.davi.demo.booking.service.exception.NotFoundException;
import com.davi.demo.booking.service.exception.ValidationException;
import com.davi.demo.booking.service.model.Property;
import com.davi.demo.booking.service.repository.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {
    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private PropertyService propertyService;

    @Test
    public void givenValidId_whenGetPropertyById_thenReturnProperty() {
        Long id = 1L;
        var property = new Property();
        property.setId(id);

        when(propertyRepository.findById(id))
                .thenReturn(Optional.of(property));

        Property resultProperty = propertyService.getPropertyById(id);

        assertThat(resultProperty).isEqualTo(property);
    }

    @Test
    public void givenNullId_whenGetPropertyById_thenThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            propertyService.getPropertyById(null);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Property Id is required");
    }

    @Test
    public void givenNotExistingId_whenGetPropertyById_thenThrowNotFoundException() {
        when(propertyRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            propertyService.getPropertyById(99L);
        });

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Property id: 99 not found");
    }
}