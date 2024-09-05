package com.finpro.roomio_backend.users.controller;

import com.finpro.roomio_backend.exceptions.image.ImageNotFoundException;
import com.finpro.roomio_backend.image.entity.ImageUserAvatar;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadResponseDto;
import com.finpro.roomio_backend.responses.Response;
import com.finpro.roomio_backend.users.entity.dto.VerifyPasswordRequestDto;
import com.finpro.roomio_backend.users.entity.dto.VerifyPasswordResponseDto;
import com.finpro.roomio_backend.users.entity.dto.UserProfileDto;
import com.finpro.roomio_backend.users.entity.dto.changePassword.ChangePasswordRequestDto;
import com.finpro.roomio_backend.users.entity.dto.userManagement.ProfileUpdateRequestDTO;
import com.finpro.roomio_backend.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // * Get logged in user's profile
    @GetMapping("/me")
    public ResponseEntity<Response<UserProfileDto>> getUserProfile() {
        UserProfileDto userProfile = userService.getProfile();
        if (userProfile != null) {
            return Response.successfulResponse(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), userProfile);
        } else {
            return Response.failedResponse(HttpStatus.NOT_FOUND.value(), "There is no user profile", null);
        }
    }

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody VerifyPasswordRequestDto request) {
        String currentUserEmail = userService.getCurrentUserEmail();
        boolean isPasswordValid = userService.verifyPassword(currentUserEmail, request.getPassword());
        return ResponseEntity.ok(new VerifyPasswordResponseDto(isPasswordValid));
    }


    // * upload image
    @PostMapping("/me/image/upload")
    public ResponseEntity<Response<ImageUploadResponseDto>> uploadImage(ImageUploadRequestDto requestDto) {
        ImageUserAvatar uploadedImageUserAvatar = userService.uploadAvatar(requestDto);
        if (uploadedImageUserAvatar == null) {
            return ResponseEntity.noContent().build();
        } else {
            return Response.successfulResponse(HttpStatus.OK.value(), "Image success uploaded!", new ImageUploadResponseDto(
                    uploadedImageUserAvatar));
        }
    }

    // * Edit Profile
    @PutMapping("/me/update")
    public ResponseEntity<Response<UserProfileDto>> updateUserProfile(@Valid @RequestBody ProfileUpdateRequestDTO requestDTO)
            throws ImageNotFoundException {
        userService.update(requestDTO);
        UserProfileDto userProfile = userService.getProfile();
        return Response.successfulResponse(HttpStatus.OK.value(), "Profile update successful!! :D", userProfile);
    }

    // * Change Password
    @PutMapping("me/change-password")
    public ResponseEntity<Response<Void>> changePassword(@Valid @RequestBody ChangePasswordRequestDto requestDto) {
        userService.changePassword(requestDto);
        return Response.successfulResponse(HttpStatus.OK.value(),"Password change successful!", null);
    }

}