package com.finpro.roomio_backend.properties.entity.dto;

import com.finpro.roomio_backend.properties.entity.Rooms;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomResponseDto {

    private Long id;
    private String name;
    private String description;
    private Integer capacity;
    private Integer size;
    private String bedType;
    private Integer totalBed;
    private Integer qty;
    private BigDecimal basePrice;
    private Integer totalBathroom;
    private Boolean isActive;
    private BigDecimal currentPrice;
    private BigDecimal actualPrice;

    public RoomResponseDto(Rooms room) {
        this.id = room.getId();
        this.name = room.getName();
        this.description = room.getDescription();
        this.capacity = room.getCapacity();
        this.size = room.getSize();
        this.bedType = room.getBedType();
        this.totalBed = room.getTotalBed();
        this.qty = room.getQty();
        this.basePrice = room.getBasePrice();
        this.totalBathroom = room.getTotalBathroom();
        this.isActive = room.getIsActive();
        this.actualPrice = room.getActualPrice(); // Use the method to get the actual price
    }
}