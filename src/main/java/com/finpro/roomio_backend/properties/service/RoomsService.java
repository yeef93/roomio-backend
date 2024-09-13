package com.finpro.roomio_backend.properties.service;

import com.finpro.roomio_backend.properties.entity.dto.RoomRequestDto;
import com.finpro.roomio_backend.properties.entity.dto.RoomResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface RoomsService {

    RoomResponseDto addRoom(Long propertyId, RoomRequestDto roomDTO);
    List<RoomResponseDto> getRoomsByPropertyId(Long propertyId);
    ResponseEntity<RoomResponseDto> getRoomById(Long propertyId, Long roomId);
    RoomResponseDto updateRoom(Long propertyId, Long roomId, RoomRequestDto roomRequestDto);
    RoomResponseDto deactivateRoom(Long propertyId, Long roomId);
}
