package com.finpro.roomio_backend.users.service;

import com.finpro.roomio_backend.image.entity.ImageUserAvatar;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.entity.dto.UserProfileDto;
import com.finpro.roomio_backend.users.entity.dto.changePassword.ChangePasswordRequestDto;
import com.finpro.roomio_backend.users.entity.dto.userManagement.ProfileUpdateRequestDTO;

import java.util.Optional;

public interface UserService {
    Optional<Users> getUserByEmail(String email);

    Users getByUsername(String username);

    // getting logged-in user
    Users getCurrentUser();

    // Getting Users
    UserProfileDto getProfile();

    boolean verifyPassword(String email, String rawPassword);

    // uploading picture per user
    ImageUserAvatar uploadAvatar(ImageUploadRequestDto requestDto);

    String getCurrentUserEmail();

    void changePassword(ChangePasswordRequestDto requestDto);

    void update(ProfileUpdateRequestDTO requestDto);
}
