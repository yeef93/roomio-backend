package com.finpro.roomio_backend.properties.service;

import com.finpro.roomio_backend.properties.entity.dto.RoomRequestDto;
import com.finpro.roomio_backend.properties.entity.dto.RoomResponseDto;

public interface RoomsService {

    RoomResponseDto addRoom(Long propertyId, RoomRequestDto roomDTO);
}
