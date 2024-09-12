package com.finpro.roomio_backend.properties.entity.dto;

import com.finpro.roomio_backend.image.entity.dto.ImagePropertiesListDto;
import com.finpro.roomio_backend.properties.entity.Properties;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class PropertiesResponseDto {

    private Long id;
    private String name;
    private String description;
    private String location;
    private String city;
    private String map;
    private String category;
    private List<ImagePropertiesListDto> images; // Add this field for images


    // Constructor that accepts a Properties entity
    public PropertiesResponseDto(Properties properties) {
        this.id = properties.getId();
        this.name = properties.getName();
        this.description = properties.getDescription();
        this.location = properties.getLocation();
        this.city = properties.getCity();
        this.map = properties.getMap();
        this.category = properties.getCategories().getName();
        // Map the list of ImageProperties to a list of ImageDto
        this.images = properties.getImages().stream()
                .map(ImagePropertiesListDto::new)
                .collect(Collectors.toList());
    }

    // Utility method to map Properties entity to PropertiesResponseDto
    public PropertiesResponseDto toDto(Properties properties) {
        return new PropertiesResponseDto(properties);
    }
}
