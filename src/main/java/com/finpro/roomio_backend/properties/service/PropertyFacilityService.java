package com.finpro.roomio_backend.properties.service;

import com.finpro.roomio_backend.properties.entity.dto.FacilitiesResponseDto;

import java.util.List;

public interface PropertyFacilityService {
    List<FacilitiesResponseDto> addFacilitiesToProperty(Long propertyId, List<Integer> facilityIds);
    List<FacilitiesResponseDto> getFacilitiesForProperty(Long propertyId);
}
