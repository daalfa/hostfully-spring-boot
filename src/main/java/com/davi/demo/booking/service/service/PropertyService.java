package com.davi.demo.booking.service.service;

import com.davi.demo.booking.service.exception.NotFoundException;
import com.davi.demo.booking.service.exception.ValidationException;
import com.davi.demo.booking.service.model.Property;
import com.davi.demo.booking.service.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    /**
     * Find Property by id and Validate if id is not null
     */
    public Property getPropertyById(Long id) {
        if(id == null) {
            throw new ValidationException("Property Id is required");
        }
        return propertyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Property id: {0,number,#} not found", id));
    }
}
