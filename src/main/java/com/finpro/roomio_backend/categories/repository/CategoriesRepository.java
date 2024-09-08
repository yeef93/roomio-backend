package com.finpro.roomio_backend.categories.repository;

import com.finpro.roomio_backend.categories.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Categories, Long> {
}
