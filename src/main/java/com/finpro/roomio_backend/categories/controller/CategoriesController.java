package com.finpro.roomio_backend.categories.controller;
import com.finpro.roomio_backend.categories.service.CategoriesService;
import com.finpro.roomio_backend.image.entity.ImageCategories;
import com.finpro.roomio_backend.image.entity.dto.CategoryImageResponseDto;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;
import com.finpro.roomio_backend.responses.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/categories")
public class CategoriesController {

    private final CategoriesService categoriesService;

    public CategoriesController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    // * upload image
    @PostMapping("/image/upload")
    public ResponseEntity<Response<CategoryImageResponseDto>> uploadImage(ImageUploadRequestDto requestDto) {
        ImageCategories uploadedImageCategories = categoriesService.uploadImage(requestDto);
        if (uploadedImageCategories == null) {
            return ResponseEntity.noContent().build();
        } else {
            return Response.successfulResponse(HttpStatus.OK.value(), "Image success uploaded!", new CategoryImageResponseDto(
                    uploadedImageCategories));
        }
    }




}
