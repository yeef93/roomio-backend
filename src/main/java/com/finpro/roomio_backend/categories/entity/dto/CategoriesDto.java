package com.finpro.roomio_backend.categories.entity.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CategoriesDto {
    private String name;
    private String description;
    private MultipartFile image;
}
