package com.finpro.roomio_backend.categories.service.impl;

import com.finpro.roomio_backend.categories.entity.Categories;
import com.finpro.roomio_backend.categories.entity.dto.CategoriesRequestDto;
import com.finpro.roomio_backend.categories.repository.CategoriesRepository;
import com.finpro.roomio_backend.categories.service.CategoriesService;
import com.finpro.roomio_backend.image.entity.ImageCategories;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;
import com.finpro.roomio_backend.image.service.ImageService;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.repository.UsersRepository;
import com.finpro.roomio_backend.users.service.UsersService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;


@Service
public class CategoriesServiceImpl implements CategoriesService {

    private final CategoriesRepository categoriesRepository;
    private final UsersRepository usersRepository;
    private final ImageService imageService;
    private final UsersService usersService;

    public CategoriesServiceImpl(CategoriesRepository categoriesRepository, UsersRepository usersRepository, ImageService imageService, UsersService usersService) {
        this.categoriesRepository = categoriesRepository;
        this.usersRepository = usersRepository;
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

    @Transactional
    @Override
    public Categories createCategory(CategoriesRequestDto requestDto) {
        Users tenant = usersService.getCurrentUser();

        // Check if the current user is allowed to create a category
        if (!tenant.getIsTenant()) {
            throw new AccessDeniedException("You do not have permission to create categories.");
        }

        // Check if the category name already exists
        Optional<Categories> existingCategory = getCategoryByName(requestDto.getName());
        if (existingCategory.isPresent()) {
            throw new IllegalArgumentException("Category with name '" + requestDto.getName() + "' already exists.");
        }

        // Create a new category
        Categories category = new Categories();
        category.setName(requestDto.getName());
        category.setDescription(requestDto.getDescription());

        // Set imageCategories if provided
        Optional<ImageCategories> imageOptional = Optional.ofNullable(imageService.findById(requestDto.getImageId()));
        imageOptional.ifPresent(category::setImageCategories);

        category.setUser(tenant);  // Associate category with the current user
        return categoriesRepository.save(category);
    }


    // Get all categories
    @Override
    public List<Categories> getAllCategories() {
        return categoriesRepository.findAll();
    }

    // Get a category by ID
    @Override
    public Optional<Categories> getCategoryById(Long id) {
        return categoriesRepository.findById(id);
    }

    // Get a category by name
    @Override
    public Optional<Categories> getCategoryByName(String name) {
        return categoriesRepository.findByName(name);
    }

    @Override
    public Optional<Categories> updateCategory(Long id, CategoriesRequestDto requestDto) {
        Optional<Categories> existingCategory = categoriesRepository.findById(id);

        Users tenant = usersService.getCurrentUser();

        // Check if the current user is allowed to create a category
        if (!tenant.getIsTenant()) {
            throw new AccessDeniedException("You do not have permission to create categories.");
        }

        if (existingCategory.isPresent()) {
            Categories category = existingCategory.get();
            category.setName(requestDto.getName());
            category.setDescription(requestDto.getDescription());
            // Set imageCategories if provided
            Optional<ImageCategories> imageOptional = Optional.ofNullable(imageService.findById(requestDto.getImageId()));
            imageOptional.ifPresent(category::setImageCategories);
            category.setUser(tenant);  // Associate category with the current user
            return Optional.of(categoriesRepository.save(category));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteCategory(Long id) {
        Optional<Categories> category = categoriesRepository.findById(id);
        if (category.isPresent()) {
            categoriesRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
