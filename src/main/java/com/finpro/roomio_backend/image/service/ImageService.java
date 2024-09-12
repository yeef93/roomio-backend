package com.finpro.roomio_backend.image.service;


import com.finpro.roomio_backend.image.entity.ImageCategories;
import com.finpro.roomio_backend.image.entity.ImageProperties;
import com.finpro.roomio_backend.image.entity.ImageUserAvatar;
import com.finpro.roomio_backend.image.entity.dto.ImageUploadRequestDto;
import com.finpro.roomio_backend.image.entity.dto.PropertiesImageResponseDto;
import com.finpro.roomio_backend.users.entity.Users;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ImageService {

  ImageUserAvatar uploadAvatar(ImageUploadRequestDto imageUploadRequestDto, Users user);

  ImageUserAvatar getAvatarById(Long imageId);

  void saveAvatar(ImageUserAvatar imageUserAvatar);

  ImageCategories uploadCategories(ImageUploadRequestDto imageUploadRequestDto, Users user);

  Optional<ImageCategories> findImageById(Long imageId);

  ImageCategories findById(Long imageId);

  List<ImageProperties> uploadPropertyImage(Long propertyId, List<MultipartFile> files, Users user);

  List<PropertiesImageResponseDto> getImagesByPropertyId(Long propertyId);
}
