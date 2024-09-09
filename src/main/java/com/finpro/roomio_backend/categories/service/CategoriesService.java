package com.finpro.roomio_backend.categories.service;

import com.finpro.roomio_backend.categories.entity.Categories;
import com.finpro.roomio_backend.categories.entity.dto.CategoriesRequestDto;
import com.finpro.roomio_backend.image.entity.ImageCategories;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;

import java.util.List;
import java.util.Optional;

public interface CategoriesService {
    // uploading picture per category
    ImageCategories uploadImage(ImageUploadRequestDto requestDto);

    // create a new category
    Categories createCategory(CategoriesRequestDto category);

    // get all categories
    List<Categories> getAllCategories();

    // get category by ID
    Optional<Categories> getCategoryById(Long id);

    // get category by name
    Optional<Categories> getCategoryByName(String name);

}
