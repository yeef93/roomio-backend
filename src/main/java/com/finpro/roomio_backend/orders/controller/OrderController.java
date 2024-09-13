package com.finpro.roomio_backend.orders.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finpro.roomio_backend.orders.dto.CreateOrderDTO;
import com.finpro.roomio_backend.orders.dto.OrderDetailsDTO;
import com.finpro.roomio_backend.orders.entity.Orders;
import com.finpro.roomio_backend.orders.service.OrderService;
import com.finpro.roomio_backend.responses.Response;
import com.midtrans.httpclient.error.MidtransError;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Response<Object>> createOrderManualTransfer(@RequestBody CreateOrderDTO createOrderDTO, Authentication authentication){
        OrderDetailsDTO response = orderService.createOrderManualTransfer(createOrderDTO, authentication);
        return Response.successfulResponse("Order created successfully.", response);
    }

    @PutMapping("/{orderId}/payment-proof")
    public ResponseEntity<Response<Object>> uploadPaymentProof(Authentication authentication, @PathVariable Long orderId, @RequestParam("file")MultipartFile file) throws IOException {
        OrderDetailsDTO response = orderService.uploadPaymentProof(authentication, orderId, file);
        return Response.successfulResponse("Payment proof successfully uploaded", response);
    }

    @PostMapping("/online-payment")
    public ResponseEntity<Response<String>> createOrderMidtrans(@RequestBody CreateOrderDTO createOrderDTO, Authentication authentication) throws MidtransError {
        String response = orderService.createOrderMidtrans(createOrderDTO, authentication);
        return Response.successfulResponse("Order created successfully.", response);
    }

    @PostMapping("/midtrans-notification")
    public ResponseEntity<Response<Object>> handleMidtransNotification(@RequestBody String notificationString) {
        try {
            // Log the raw notification data for debugging
            System.out.println("Received raw notification: " + notificationString);

            // Parse the received JSON string into a Map
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> notificationData = objectMapper.readValue(notificationString, new TypeReference<Map<String, Object>>() {});

            // Pass the parsed map to the service
            String response = orderService.handleMidtransNotification(notificationData);
            return Response.successfulResponse(response);
        } catch (Exception e) {
            // Handle JSON parsing or other errors
            return Response.failedResponse("Error processing notification: " + e.getMessage());
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Response<Object>> userCancelOrder(Authentication authentication, @PathVariable Long orderId) {
        String response = orderService.userCancelOrder(authentication, orderId);
        return Response.successfulResponse(response);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<Orders>> userGetOrders(
            Authentication authentication,
            @RequestParam Integer statusId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Optional<Long> optionalOrderId = Optional.ofNullable(orderId);
        Optional<LocalDate> optionalStartDate = startDate != null ? Optional.of(LocalDate.parse(startDate)) : Optional.empty();
        Optional<LocalDate> optionalEndDate = endDate != null ? Optional.of(LocalDate.parse(endDate)) : Optional.empty();

        // Get filtered orders with pagination
        Page<Orders> orders = orderService.userGetOrders(authentication, statusId, optionalOrderId, optionalStartDate, optionalEndDate, page, size);
        return ResponseEntity.ok(orders);
    }


}
