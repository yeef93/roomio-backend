package com.finpro.roomio_backend.categories.controller;
import com.finpro.roomio_backend.categories.entity.Categories;
import com.finpro.roomio_backend.categories.entity.dto.CategoriesRequestDto;
import com.finpro.roomio_backend.categories.entity.dto.CategoriesResponseDto;
import com.finpro.roomio_backend.categories.service.CategoriesService;
import com.finpro.roomio_backend.image.entity.ImageCategories;
import com.finpro.roomio_backend.image.entity.dto.CategoryImageResponseDto;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;
import com.finpro.roomio_backend.responses.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;


@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
public class CategoriesController {

    private final CategoriesService categoriesService;

    public CategoriesController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    // * upload image
    @PostMapping("/image/upload")
    public ResponseEntity<Response<CategoryImageResponseDto>> uploadImage(@RequestBody ImageUploadRequestDto requestDto) {
        ImageCategories uploadedImageCategories = categoriesService.uploadImage(requestDto);
        if (uploadedImageCategories == null) {
            return ResponseEntity.noContent().build();
        } else {
            return Response.successfulResponse(HttpStatus.OK.value(), "Image successfully uploaded!", new CategoryImageResponseDto(uploadedImageCategories));
        }
    }

    // Create a new category
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CategoriesRequestDto requestDto) {
        try {
            // Create category using service
            Categories createdCategory = categoriesService.createCategory(requestDto);

            // Convert the created category to the response DTO
            CategoriesResponseDto responseDto = new CategoriesResponseDto(createdCategory);

            // Return success response with the DTO
            return Response.failedResponse(
                    CREATED.value(),
                    "Category successfully created!",
                    responseDto);
        } catch (IllegalArgumentException e) {
            // Return a custom error response when category name already exists
            return Response.failedResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    e.getMessage(),
                    null
            );
        } catch (Exception e) {
            // Catch-all for other exceptions
            return Response.failedResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected error occurred.",
                    null
            );
        }
    }

    // Get all categories
    @GetMapping
    public ResponseEntity<Response<List<Categories>>> getAllCategories() {
        List<Categories> categories = categoriesService.getAllCategories();
        return Response.successfulResponse(HttpStatus.OK.value(), "Categories retrieved successfully", categories);
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<Response<Categories>> getCategoryById(@PathVariable Long id) {
        Optional<Categories> category = categoriesService.getCategoryById(id);
        return category.map(value -> Response.successfulResponse(HttpStatus.OK.value(), "Category found", value))
                .orElseGet(() -> Response.failedResponse(HttpStatus.NOT_FOUND.value(), "Category not found"));
    }

}
