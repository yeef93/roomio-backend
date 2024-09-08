package com.finpro.roomio_backend.image.repository;

import com.finpro.roomio_backend.image.entity.ImageCategories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageCategoriesRepository extends JpaRepository<ImageCategories, Long> {
}
