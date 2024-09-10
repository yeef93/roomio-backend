package com.finpro.roomio_backend.image.repository;


import com.finpro.roomio_backend.image.entity.ImageUserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageUserAvatarRepository extends JpaRepository<ImageUserAvatar, Long> {

}
