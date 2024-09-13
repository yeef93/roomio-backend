package com.finpro.roomio_backend.image.repository;

import com.finpro.roomio_backend.image.entity.ImageRoom;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ImageRoomRepository extends JpaRepository<ImageRoom, Long> {
}
