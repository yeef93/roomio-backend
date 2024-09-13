package com.finpro.roomio_backend.properties.entity.dto;

import com.finpro.roomio_backend.image.entity.dto.ImagePropertiesListDto;
import com.finpro.roomio_backend.properties.entity.Properties;
import lombok.Data;

import java.time.Instant;
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
    private TenantDto tenant; // Updated to hold tenant information as an object
    private List<ImagePropertiesListDto> images; // List of image DTOs
    private List<RoomResponseDto> rooms;

    // Constructor that accepts a Properties entity
    public PropertiesResponseDto(Properties properties) {
        this.id = properties.getId();
        this.name = properties.getName();
        this.description = properties.getDescription();
        this.location = properties.getLocation();
        this.city = properties.getCity();
        this.map = properties.getMap();
        this.category = properties.getCategories().getName();

        // Map tenant data including first name, last name, avatar, and created date
        this.tenant = new TenantDto(
                properties.getUser().getId(),
                properties.getUser().getEmail(),
                properties.getUser().getFirstname(),
                properties.getUser().getLastname(),
                properties.getUser().getAvatar().getImageUrl(),
                properties.getUser().getCreatedAt()
        );

        // Map the list of ImageProperties to a list of ImagePropertiesListDto
        this.images = properties.getImages().stream()
                .map(ImagePropertiesListDto::new)
                .collect(Collectors.toList());

        // Map the list of rooms to a list of RoomDto
        this.rooms = properties.getRooms().stream()
                .map(RoomResponseDto::new)
                .collect(Collectors.toList());
    }

    // Utility method to map Properties entity to PropertiesResponseDto
    public PropertiesResponseDto toDto(Properties properties) {
        return new PropertiesResponseDto(properties);
    }

    // Nested class for Tenant data
    @Data
    public static class TenantDto {
        private Long id;
        private String email;
        private String firstname;
        private String lastname;
        private String avatar;  // Avatar image URL or path
        private Instant createdAt;

        public TenantDto(Long id, String email, String firstname, String lastname, String avatar, Instant createdAt) {
            this.id = id;
            this.email = email;
            this.firstname = firstname;
            this.lastname = lastname;
            this.avatar = avatar;
            this.createdAt = createdAt;
        }
    }
}
