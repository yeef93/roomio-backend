package com.finpro.roomio_backend.properties.controller;


import com.finpro.roomio_backend.exceptions.ObjectNotFoundException;
import com.finpro.roomio_backend.image.entity.ImageProperties;
import com.finpro.roomio_backend.image.entity.ImageRoom;
import com.finpro.roomio_backend.image.entity.dto.PropertiesImageResponseDto;
import com.finpro.roomio_backend.image.entity.dto.RoomImageResponseDto;
import com.finpro.roomio_backend.image.service.ImageService;
import com.finpro.roomio_backend.properties.entity.Properties;
import com.finpro.roomio_backend.properties.entity.Rooms;
import com.finpro.roomio_backend.properties.entity.dto.*;
import com.finpro.roomio_backend.properties.service.PropertiesService;
import com.finpro.roomio_backend.properties.service.PropertyFacilityService;
import com.finpro.roomio_backend.properties.service.RoomsService;
import com.finpro.roomio_backend.responses.Response;
import com.finpro.roomio_backend.users.entity.Users;
import com.finpro.roomio_backend.users.service.UsersService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/property")
public class PropertiesController {

    private final PropertiesService propertiesService;
    private final ImageService imageService;
    private final UsersService usersService;
    private final RoomsService roomsService;
    private final PropertyFacilityService propertyFacilityService;

    public PropertiesController(PropertiesService propertiesService, ImageService imageService,
                                UsersService usersService, PropertyFacilityService propertyFacilityService,
                                RoomsService roomsService) {
        this.propertiesService = propertiesService;
        this.imageService = imageService;
        this.usersService = usersService;
        this.propertyFacilityService = propertyFacilityService;
        this.roomsService = roomsService;
    }

    // * upload image
    @PostMapping(value = "/{propertyId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<List<PropertiesImageResponseDto>>> uploadImages( @PathVariable Long propertyId, @RequestParam("files") List<MultipartFile> files) {
        try {
            Users tenant = usersService.getCurrentUser();
            if (!tenant.getIsTenant()) {
                throw new AccessDeniedException("You do not have permission to upload an image for property");
            }
            // Call the service method to upload images
            List<ImageProperties> uploadedImages = imageService.uploadPropertyImage(propertyId, files, tenant);
            if (uploadedImages.isEmpty()) {
                return ResponseEntity.noContent().build();
            } else {
                List<PropertiesImageResponseDto> responseDtos = uploadedImages.stream()
                        .map(PropertiesImageResponseDto::new)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(Response.successfulResponse(OK.value(), "Property Images successfully uploaded!", responseDtos).getBody());
            }
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Failed to upload property images: " + e.getMessage());
        }
    }

    // Endpoint to get images by property ID
    @GetMapping("/{propertyId}/images")
    public ResponseEntity<Response<List<PropertiesImageResponseDto>>> getPropertyImages(@PathVariable Long propertyId) {
        try {
            List<PropertiesImageResponseDto> images = imageService.getImagesByPropertyId(propertyId);
            return ResponseEntity.ok(Response.successfulResponse(OK.value(), "Property Images successfully retrieved!", images).getBody());
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Failed to get property images: " + e.getMessage());
        }
    }


    // Create a new property
    @PostMapping
    public ResponseEntity<?> createProperty(@RequestBody PropertiesRequestDto requestDto) {
        try {
            // Create property using service
            Properties createdProperty = propertiesService.createProperty(requestDto);

            // Convert the created property to the response DTO
            PropertiesResponseDto responseDto = new PropertiesResponseDto(createdProperty);

            // Return success response with the DTO
            return Response.successfulResponse(
                    CREATED.value(),
                    "Property successfully created!",
                    responseDto);
        } catch (IllegalArgumentException e) {
            // Return a custom error response when property name already exists
            return Response.failedResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    e.getMessage(),
                    null
            );
        } catch (Exception e) {
            // Catch-all for other exceptions
            return Response.failedResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected error occurred.",
                    null
            );
        }
    }

//    // Get all Property
//    @GetMapping
//    public ResponseEntity<?> getAllProperty() {
//        try {
//            List<Properties> properties = propertiesService.getAllProperties();
//            // Convert List of Property to List of PropertyResponseDto
//            List<PropertiesResponseDto> propertyDtos = properties.stream()
//                    .map(PropertiesResponseDto::new)
//                    .collect(Collectors.toList());
//            // Return the response
//            return Response.successfulResponse(OK.value(),"Property retrieved successfully", propertyDtos);
//        } catch (Exception e) {
//            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Failed to get all property: " + e.getMessage());
//        }
//    }

    // Get all Property with search, sort, pagination, and city filtering
    @GetMapping
    public ResponseEntity<?> getAllProperty(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            // Call the service with the search, city, sort, and pagination parameters
            Page<Properties> propertiesPage = propertiesService.getProperties(search, city, sortBy, direction, page, size);

            // Convert List of Property to List of PropertyResponseDto
            List<PropertiesResponseDto> propertyDtos = propertiesPage.getContent().stream()
                    .map(PropertiesResponseDto::new)
                    .collect(Collectors.toList());

            // Return the response with pagination details
            return Response.successfulResponse(
                    HttpStatus.OK.value(),
                    "Properties retrieved successfully",
                    propertyDtos,
                    propertiesPage.getTotalPages(),
                    propertiesPage.getTotalElements()
            );
        } catch (Exception e) {
            return Response.failedResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Failed to get properties: " + e.getMessage()
            );
        }
    }



    // Get property by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyById(@PathVariable Long id) {
        try {
            Optional<Properties> property = propertiesService.getPropertyById(id);
            // Map the entity to DTO and return the response
            return property.map(value -> {
                        PropertiesResponseDto propertyDto = new PropertiesResponseDto(value);
                        return Response.successfulResponse(HttpStatus.OK.value(),"Property found",propertyDto);
                    })
                    .orElseGet(() -> Response.failedResponse(HttpStatus.NOT_FOUND.value(),"Property not found"
                    ));
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Failed to get property: " + e.getMessage());
        }
    }

    // Update an existing property by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProperty(@PathVariable Long id,@RequestBody PropertiesRequestDto requestDto) {
        try {
            Optional<Properties> updateProperty = propertiesService.updateProperty(id, requestDto);

            // Check if property was found and updated
            if (updateProperty.isPresent()) {
                PropertiesResponseDto responseDto = new PropertiesResponseDto(updateProperty.get());
                return Response.successfulResponse(HttpStatus.OK.value(), "Property successfully updated!", responseDto);
            } else {
                return Response.failedResponse(HttpStatus.NOT_FOUND.value(), "Property not found", null);
            }
        } catch (IllegalArgumentException e) {
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred.", null);
        }
    }

    // Delete a property by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProperty(@PathVariable Long id) {
        try {
            boolean isDeleted = propertiesService.deleteProperty(id);
            if (isDeleted) {
                return Response.successfulResponse(HttpStatus.OK.value(), "Property successfully deleted", null);
            } else {
                return Response.failedResponse(HttpStatus.NOT_FOUND.value(), "Property not found", null);
            }
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred.", null);
        }
    }

    // Add property facility
    @PostMapping("/{propertyId}/facilities")
    public ResponseEntity<?> addFacilitiesToProperty( @PathVariable Long propertyId, @RequestBody AddFacilityRequestDto requestDto) {
        try {
            // Call the service to add facilities to the property and get the added facilities
            List<FacilitiesResponseDto> facilities = propertyFacilityService.addFacilitiesToProperty(propertyId, requestDto.getFacilityIds());
            // Return a success response
            return Response.successfulResponse(HttpStatus.CREATED.value(), "Facilities successfully added to the property!", facilities);
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Failed to add facilities: " + e.getMessage());
        }
    }

    // get property facility
    @GetMapping("/{propertyId}/facilities")
    public ResponseEntity<?> getFacilitiesForProperty(@PathVariable Long propertyId) {
        try {
            // Call the service to get the list of facilities
            List<FacilitiesResponseDto> facilities = propertyFacilityService.getFacilitiesForProperty(propertyId);

            // Return the response with the list of facilities
            return Response.successfulResponse(HttpStatus.OK.value(), "Facilities retrieved successfully!", facilities);
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Failed to get facilities: " + e.getMessage());
        }
    }


    @PostMapping("/{propertyId}/rooms")
    public ResponseEntity<?> addRoom(@PathVariable Long propertyId, @RequestBody RoomRequestDto roomDTO) {
        try {
            RoomResponseDto roomResponse = roomsService.addRoom(propertyId, roomDTO);
            return Response.successfulResponse(HttpStatus.CREATED.value(), "Room successfully added to the property!", roomResponse);
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Failed to add room: " + e.getMessage());
        }
    }

    // get all property room
    @GetMapping("/{propertyId}/rooms")
    public ResponseEntity<?> getRooms(@PathVariable Long propertyId) {
        try {
            List<RoomResponseDto> rooms = roomsService.getRoomsByPropertyId(propertyId);
            return Response.successfulResponse(OK.value(), "Rooms successfully retrieved.", rooms);
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Failed to get room: " + e.getMessage());
        }
    }

//    // get all property room
//    @GetMapping("/{propertyId}/rooms")
//    public ResponseEntity<?> getRoomsByProperty(@PathVariable Long propertyId) {
//        try {
//            List<RoomResponseDto> rooms = propertiesService.getRoomsWithPeakRates(propertyId);
//            return ResponseEntity.ok(rooms);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Failed to get rooms: " + e.getMessage());
//        }
//    }

    //get property room by id
    @GetMapping("/{propertyId}/rooms/{roomId}")
    public ResponseEntity<?> getRoomById(@PathVariable Long propertyId, @PathVariable Long roomId) {
        try {
            RoomResponseDto roomResponse = roomsService.getRoomById(propertyId, roomId).getBody();  // Call the service method
            return Response.successfulResponse(HttpStatus.OK.value(), "Room successfully retrieved.", roomResponse);
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.NOT_FOUND.value(), "Failed to get room: " + e.getMessage());
        }
    }

    //update room details
    @PutMapping("/{propertyId}/rooms/{roomId}")
    public ResponseEntity<?> updateRoom(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @RequestBody RoomRequestDto roomRequestDto) {

        try {
            RoomResponseDto updatedRoom = roomsService.updateRoom(propertyId, roomId, roomRequestDto);
            return Response.successfulResponse(HttpStatus.OK.value(), "Room updated successfully.", updatedRoom);
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(), "Failed to update room: " + e.getMessage());
        }
    }

    //deactivate room property
    @PutMapping("/{propertyId}/rooms/{roomId}/inactive")
    public ResponseEntity<?> deactivateRoom(@PathVariable Long propertyId, @PathVariable Long roomId) {
        try {
            RoomResponseDto updatedRoom = roomsService.deactivateRoom(propertyId, roomId);
            return Response.successfulResponse(HttpStatus.OK.value(), "Room successfully deactivated.", updatedRoom);
        } catch (ObjectNotFoundException e) {
            return Response.failedResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        } catch (Exception e) {
            return Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to deactivate room.");
        }
    }


    //upload room image
    @PostMapping("/{propertyId}/rooms/{roomId}/images")
    public ResponseEntity<Response<List<RoomImageResponseDto>>> uploadRoomImages(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal Users user) {
        try {
            Users tenant = usersService.getCurrentUser();
            if (!tenant.getIsTenant()) {
                throw new AccessDeniedException("You do not have permission to upload an image for property");
            }
            // Call the service method to upload images
            List<ImageRoom> uploadedImages = imageService.uploadRoomImage(roomId, files, user);
            // Return a successful response with the list of uploaded images
            if (uploadedImages.isEmpty()) {
                return ResponseEntity.noContent().build();
            } else {
                List<RoomImageResponseDto> responseDtos = uploadedImages.stream()
                        .map(RoomImageResponseDto::new)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(Response.successfulResponse(OK.value(), "Room Images successfully uploaded!", responseDtos).getBody());
            }
        } catch (IllegalArgumentException e) {
            // Handle the case where the room is not found
            return Response.failedResponse(HttpStatus.BAD_REQUEST.value(),e.getMessage());
        } catch (Exception e) {
            // Handle any other errors
            return Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Image upload failed: " + e.getMessage());
        }
    }



}
