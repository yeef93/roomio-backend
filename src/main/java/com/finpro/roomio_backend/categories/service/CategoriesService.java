package com.finpro.roomio_backend.categories.service;

import com.finpro.roomio_backend.image.entity.ImageCategories;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;

public interface CategoriesService {
    // uploading picture per category
    ImageCategories uploadImage(ImageUploadRequestDto requestDto);

}
