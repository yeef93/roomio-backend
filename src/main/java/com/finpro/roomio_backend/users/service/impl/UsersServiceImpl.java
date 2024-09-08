package com.finpro.roomio_backend.users.service.impl;

import com.finpro.roomio_backend.exceptions.image.ImageNotFoundException;
import com.finpro.roomio_backend.exceptions.user.UserNotFoundException;
import com.finpro.roomio_backend.image.entity.ImageUserAvatar;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;
import com.finpro.roomio_backend.image.service.ImageService;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.entity.dto.UserProfileDto;
import com.finpro.roomio_backend.users.entity.dto.changePassword.ChangePasswordRequestDto;
import com.finpro.roomio_backend.users.entity.dto.userManagement.ProfileUpdateRequestDTO;
import com.finpro.roomio_backend.users.repository.UsersRepository;
import com.finpro.roomio_backend.users.service.UsersService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UsersServiceImpl implements UsersService {

    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    public UsersServiceImpl(UsersRepository userRepository, PasswordEncoder passwordEncoder, ImageService imageService) {
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

    @Override
    public String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername(); // Username is typically the email
        } else {
            return principal.toString();
        }
    }

    @Override
    public void changePassword(ChangePasswordRequestDto requestDto) throws RuntimeException {
        Users loggedInUser = getCurrentUser();

        // Verify old password
        if (!passwordEncoder.matches(requestDto.getOldPassword(), loggedInUser.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        // Check if new password matches confirm password
        if (!requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        // Check if new password is the same as the old password
        if (passwordEncoder.matches(requestDto.getNewPassword(), loggedInUser.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as the old password");
        }

        // Update password
        loggedInUser.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(loggedInUser);
    }

    @Override
    public void update(ProfileUpdateRequestDTO requestDto)
            throws RuntimeException {
        Users existingUser = getCurrentUser();
        ProfileUpdateRequestDTO update = new ProfileUpdateRequestDTO();
        update.dtoToEntity(existingUser, requestDto);

        // check for image
        if (requestDto.getAvatarId() != null) {
            ImageUserAvatar avatar = imageService.getAvatarById(requestDto.getAvatarId());
            if (avatar != null) {
                existingUser.setAvatar(avatar);
            } else {
                throw new ImageNotFoundException(
                        "ImageUserAvatar doesn't exist in database. Please enter another imageId or upload a "
                                + "new image");
            }
        }
        userRepository.save(existingUser);
    }

}
