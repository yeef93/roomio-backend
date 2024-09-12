package com.finpro.roomio_backend.categories.entity.dto;

import com.finpro.roomio_backend.categories.entity.Categories;
import lombok.Data;

@Data
public class CategoriesResponseDto {
    private Long id;
    private String name;
    private String description;
    private ImageDto image;

    @Data
    public static class ImageDto {
        private Long id;
        private String imageUrl;
    }

    // Constructor that accepts a Categories entity
    public CategoriesResponseDto(Categories categories) {
        this.id = categories.getId();
        this.name = categories.getName();
        this.description = categories.getDescription();

        // Check if image exists and map to ImageDto
        if (categories.getImageCategories() != null) {
            this.image = new ImageDto();
            this.image.setId(categories.getImageCategories().getId());
            this.image.setImageUrl(categories.getImageCategories().getImageUrl());
        }
    }

    // Utility method to map Categories entity to CategoriesResponseDto
    public CategoriesResponseDto toDto(Categories categories) {
        return new CategoriesResponseDto(categories);
    }
}