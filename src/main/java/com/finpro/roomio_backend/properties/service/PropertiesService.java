package com.finpro.roomio_backend.properties.service;

import com.finpro.roomio_backend.properties.entity.Properties;
import com.finpro.roomio_backend.properties.entity.dto.PropertiesRequestDto;

import java.util.List;
import java.util.Optional;

public interface PropertiesService {

    // create a new property
    Properties createProperty(PropertiesRequestDto requestDto);

    // get all Property
    List<Properties> getAllProperties();

    // get Property by ID
    Optional<Properties> getPropertyById(Long id);

    // get Property by name
    Optional<Properties> getPropertyByName(String name);

    //update property
    Optional<Properties> updateProperty(Long id, PropertiesRequestDto requestDto);

    //delete property
    boolean deleteProperty(Long id);
}
