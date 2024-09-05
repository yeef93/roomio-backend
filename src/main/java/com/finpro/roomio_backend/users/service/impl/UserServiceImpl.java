package com.finpro.roomio_backend.users.service.impl;

import com.finpro.roomio_backend.exceptions.user.UserNotFoundException;
import com.finpro.roomio_backend.image.entity.ImageUserAvatar;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;
import com.finpro.roomio_backend.image.service.ImageService;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.entity.dto.UserProfileDto;
import com.finpro.roomio_backend.users.repository.UsersRepository;
import com.finpro.roomio_backend.users.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    public UserServiceImpl(UsersRepository userRepository, PasswordEncoder passwordEncoder, ImageService imageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageService = imageService;
    }


    @Override
    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Users getByUsername(String email) throws RuntimeException {
        Optional<Users> usersOptional = userRepository.findByEmail(email);
        return usersOptional.orElseThrow(() -> new UserNotFoundException(
                "User by email: " + email + " not found. Please ensure you've entered the correct email!"));
    }

    @Override
    public Users getCurrentUser() throws RuntimeException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("You must be logged in to access this resource");
        }
        String username = authentication.getName();
        return getByUsername(username);
    }

    @Override
    @Transactional
    public UserProfileDto getProfile() throws RuntimeException {
        Users user = getCurrentUser();
        return new UserProfileDto(user);
    }



    @Override
    public boolean verifyPassword(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    @Override
    public ImageUserAvatar uploadAvatar(ImageUploadRequestDto requestDto) throws IllegalArgumentException {
        Users user = getCurrentUser();
        return imageService.uploadAvatar(requestDto, user);
    }
}
