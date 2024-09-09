package com.finpro.roomio_backend.categories.entity.dto;

import com.finpro.roomio_backend.categories.entity.Categories;
import lombok.Data;

@Data
public class CategoriesResponseDto {
    private Long id;
    private String name;
    private String description;
    private Long imageId;
    private String imageUrl;

    @Data
    public static class ImageDto {
        private Long id;
        private String imageUrl;
    }

    public CategoriesResponseDto(Categories categories) {
        this.id = categories.getId();
        this.name = categories.getName();
        this.description = categories.getDescription();
        this.imageId = categories.getImageCategories().getId();
        this.imageUrl = categories.getImageCategories().getImageUrl();
    }

    public CategoriesResponseDto toDto(Categories categories) {
        return new CategoriesResponseDto(categories);
    }

}
