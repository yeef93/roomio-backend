package com.finpro.roomio_backend.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OrderDetailsDTO {
    private Long orderId;
//    private Integer roomId;
    private LocalDate checkin;
    private LocalDate checkout;
    private Integer numberOfPeople;
    private Integer qty;
    private BigDecimal totalPrice;
    private BigDecimal priceExcludeFees;
    private String paymentType;
    private Integer statusId;
    private String paymentProof;

}
