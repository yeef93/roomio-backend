package com.finpro.roomio_backend.properties.service.impl;

import com.finpro.roomio_backend.properties.entity.Properties;
import com.finpro.roomio_backend.properties.entity.Rooms;
import com.finpro.roomio_backend.properties.entity.dto.RoomRequestDto;
import com.finpro.roomio_backend.properties.entity.dto.RoomResponseDto;
import com.finpro.roomio_backend.properties.repository.PropertiesRepository;
import com.finpro.roomio_backend.properties.repository.RoomsRepository;
import com.finpro.roomio_backend.properties.service.RoomsService;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.service.UsersService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class RoomsServiceImpl implements RoomsService {

    private final RoomsRepository roomsRepository;
    private final PropertiesRepository propertiesRepository;
    private final UsersService usersService;


    public RoomsServiceImpl(RoomsRepository roomRepository, PropertiesRepository propertiesRepository,
                            UsersService usersService) {
        this.roomsRepository = roomRepository;
        this.propertiesRepository = propertiesRepository;
        this.usersService = usersService;
    }

    public RoomResponseDto addRoom(Long propertyId, RoomRequestDto roomDTO) {

        Users tenant = usersService.getCurrentUser();
        // Check if the current user is allowed to create a category
        if (!tenant.getIsTenant()) {
            throw new AccessDeniedException("You do not have permission to create room.");
        }

        // Fetch the property by its ID
        Properties property = propertiesRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid property ID: " + propertyId));

        Rooms room = new Rooms();
        room.setProperties(property);
        room.setName(roomDTO.getName());
        room.setDescription(roomDTO.getDescription());
        room.setCapacity(roomDTO.getCapacity());
        room.setSize(roomDTO.getSize());
        room.setBedType(roomDTO.getBedType());
        room.setTotalBed(roomDTO.getTotalBed());
        room.setQty(roomDTO.getQty());
        room.setBasePrice(roomDTO.getBasePrice());
        room.setTotalBathroom(roomDTO.getTotalBathroom());
        room.setIsActive(true);

        Rooms savedRoom = roomsRepository.save(room);
        return new RoomResponseDto(savedRoom);
    }
}
