package com.finpro.roomio_backend.image.service.impl;


import com.finpro.roomio_backend.exceptions.image.ImageNotFoundException;
import com.finpro.roomio_backend.image.entity.ImageCategories;
import com.finpro.roomio_backend.image.entity.ImageUserAvatar;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;
import com.finpro.roomio_backend.image.repository.ImageCategoriesRepository;
import com.finpro.roomio_backend.image.repository.ImageUserAvatarRepository;
import com.finpro.roomio_backend.image.service.CloudinaryService;
import com.finpro.roomio_backend.image.service.ImageService;
import com.finpro.roomio_backend.users.entity.Users;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Data
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

  private final ImageUserAvatarRepository imageUserAvatarRepository;
  private final ImageCategoriesRepository imageCategoriesRepository;
  private final CloudinaryService cloudinaryService;

  @Override
  public void saveAvatar(ImageUserAvatar imageUserAvatar) {
    imageUserAvatarRepository.save(imageUserAvatar);
  }


  @Override
  public ImageUserAvatar getAvatarById(Long imageId) {
    return imageUserAvatarRepository.findById(imageId)
        .orElseThrow(() -> new ImageNotFoundException("ImageUserAvatar not found"));
  }

  @Override
  @Transactional
  public ImageUserAvatar uploadAvatar(ImageUploadRequestDto requestDto, Users user) throws IllegalArgumentException {
    validateUploadRequest(requestDto);

    String imageName = requestDto.getFileName();
    String imageUrl = cloudinaryService.uploadFile(requestDto.getFile(),
        "roomio/users/" + user.getId().toString());

    ImageUserAvatar imageUserAvatar = new ImageUserAvatar();
    imageUserAvatar.setImageName(imageName);
    imageUserAvatar.setImageUrl(imageUrl);
    imageUserAvatar.setUser(user);
    if (imageUserAvatar.getImageUrl() == null) {
      // Handle error appropriately
      return null;
    }
    return imageUserAvatarRepository.save(imageUserAvatar);
  }


  private void validateUploadRequest(ImageUploadRequestDto requestDto) {
    if (requestDto.getFileName().isEmpty() || requestDto.getFile().isEmpty()) {
      throw new IllegalArgumentException("Invalid upload request");
    }
  }

  @Override
  @Transactional
  public ImageCategories uploadCategories(ImageUploadRequestDto requestDto, Users user) throws IllegalArgumentException {
    validateUploadRequest(requestDto);

    String imageName = requestDto.getFileName();
    String imageUrl = cloudinaryService.uploadFile(requestDto.getFile(),
            "roomio/categories/" + user.getId().toString());

    ImageCategories imageCategories = new ImageCategories();
    imageCategories.setImageName(imageName);
    imageCategories.setImageUrl(imageUrl);
    imageCategories.setUser(user);
    if (imageCategories.getImageUrl() == null) {
      // Handle error appropriately
      return null;
    }
    return imageCategoriesRepository.save(imageCategories);
  }

}
