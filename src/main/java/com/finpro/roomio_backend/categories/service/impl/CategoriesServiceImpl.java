package com.finpro.roomio_backend.categories.service.impl;

import com.finpro.roomio_backend.categories.repository.CategoriesRepository;
import com.finpro.roomio_backend.categories.service.CategoriesService;
import com.finpro.roomio_backend.image.entity.ImageCategories;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;
import com.finpro.roomio_backend.image.service.ImageService;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.service.UsersService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class CategoriesServiceImpl implements CategoriesService {

    private final CategoriesRepository categoriesRepository;
    private final ImageService imageService;
    private final UsersService usersService;

    public CategoriesServiceImpl(CategoriesRepository categoriesRepository, ImageService imageService, UsersService usersService) {
        this.categoriesRepository = categoriesRepository;
        this.imageService = imageService;
        this.usersService = usersService;
    }

    @Override
    public ImageCategories uploadImage(ImageUploadRequestDto requestDto) throws IllegalArgumentException {
        Users tenant = usersService.getCurrentUser();
        if (!tenant.getIsTenant()) {
            throw new AccessDeniedException("You do not have permission to upload an image for categories");
        }
        return imageService.uploadCategories(requestDto, tenant);
    }

}
