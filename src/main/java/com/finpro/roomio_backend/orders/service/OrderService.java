package com.finpro.roomio_backend.orders.service;

import com.finpro.roomio_backend.orders.dto.CreateOrderDTO;
import com.finpro.roomio_backend.orders.dto.OrderDetailsDTO;
import com.finpro.roomio_backend.orders.entity.Orders;
import com.midtrans.httpclient.error.MidtransError;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public interface OrderService {
    OrderDetailsDTO createOrderManualTransfer(CreateOrderDTO createOrderDTO, Authentication authentication);
    OrderDetailsDTO uploadPaymentProof(Authentication authentication, Long orderId, MultipartFile file) throws IOException;
    String createOrderMidtrans(CreateOrderDTO createOrderDTO, Authentication authentication) throws MidtransError;
    String handleMidtransNotification(Map<String, Object> notificationData);
    String userCancelOrder(Authentication authentication, Long orderId);

    Page<Orders> userGetOrders(Authentication authentication,
                               Integer statusId,
                               Optional<Long> orderId,
                               Optional<LocalDate> startDate,
                               Optional<LocalDate> endDate,
                               int page,
                               int size);
}
