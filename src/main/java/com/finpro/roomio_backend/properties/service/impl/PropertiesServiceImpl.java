package com.finpro.roomio_backend.properties.service.impl;

import com.finpro.roomio_backend.categories.entity.Categories;
import com.finpro.roomio_backend.categories.repository.CategoriesRepository;
import com.finpro.roomio_backend.categories.service.CategoriesService;
import com.finpro.roomio_backend.image.service.ImageService;
import com.finpro.roomio_backend.properties.entity.Properties;
import com.finpro.roomio_backend.properties.entity.dto.PropertiesRequestDto;
import com.finpro.roomio_backend.properties.repository.PropertiesRepository;
import com.finpro.roomio_backend.properties.service.PropertiesService;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.repository.UsersRepository;
import com.finpro.roomio_backend.users.service.UsersService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertiesServiceImpl implements PropertiesService {

    private final PropertiesRepository propertiesRepository;
    private final UsersRepository usersRepository;
    private final ImageService imageService;
    private final UsersService usersService;
    private final CategoriesService categoriesService;
    private final CategoriesRepository categoriesRepository;

    public PropertiesServiceImpl(PropertiesRepository propertiesRepository, UsersRepository usersRepository,
                                 ImageService imageService, UsersService usersService, CategoriesService categoriesService,
                                 CategoriesRepository categoriesRepository) {
        this.propertiesRepository = propertiesRepository;
        this.usersRepository = usersRepository;
        this.imageService = imageService;
        this.usersService = usersService;
        this.categoriesService = categoriesService;
        this.categoriesRepository = categoriesRepository;
    }


    @Transactional
    @Override
    public Properties createProperty(PropertiesRequestDto requestDto) {
        Users tenant = usersService.getCurrentUser();

        // Check if the current user is allowed to create a category
        if (!tenant.getIsTenant()) {
            throw new AccessDeniedException("You do not have permission to create categories.");
        }

        // Check if the property name already exists
        Optional<Properties> existingProperty = getPropertyByName(requestDto.getName());
        if (existingProperty.isPresent()) {
            throw new IllegalArgumentException("Property with name '" + requestDto.getName() + "' already exists.");
        }

        // Fetch category by id
        Categories category = categoriesRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + requestDto.getCategoryId()));

        // Create a new category
        Properties property = new Properties();
        property.setName(requestDto.getName());
        property.setDescription(requestDto.getDescription());
        property.setLocation(requestDto.getLocation());
        property.setCity(requestDto.getCity());
        property.setMap(requestDto.getMap());
        property.setCategories(category);  // Set the category to the property
        property.setUser(tenant);  // Associate category with the current user
        return propertiesRepository.save(property);
    }



    // Get all property
    @Override
    public List<Properties> getAllProperties() {
        return propertiesRepository.findAll();
    }

    // Get a property by ID
    @Override
    public  Optional<Properties> getPropertyById(Long id) {
        return propertiesRepository.findById(id);
    }

    // Get a property by name
    @Override
    public Optional<Properties> getPropertyByName(String name) {
        return propertiesRepository.findByName(name);
    }

    @Override
    public Optional<Properties> updateProperty(Long id, PropertiesRequestDto requestDto) {
        Optional<Properties> existingProperty = propertiesRepository.findById(id);

        Users tenant = usersService.getCurrentUser();

        // Check if the current user is allowed to create a category
        if (!tenant.getIsTenant()) {
            throw new AccessDeniedException("You do not have permission to create categories.");
        }

        if (existingProperty.isPresent()) {
            Properties property = existingProperty.get();
            property.setName(requestDto.getName());
            property.setDescription(requestDto.getDescription());
            // Set imageCategories if provided
//            Optional<ImageCategories> imageOptional = Optional.ofNullable(imageService.findById(requestDto.getImageId()));
//            imageOptional.ifPresent(property::setImageCategories);
            property.setUser(tenant);  // Associate category with the current user
            return Optional.of(propertiesRepository.save(property));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteProperty(Long id) {
        Optional<Properties> property = propertiesRepository.findById(id);
        if (property.isPresent()) {
            propertiesRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
