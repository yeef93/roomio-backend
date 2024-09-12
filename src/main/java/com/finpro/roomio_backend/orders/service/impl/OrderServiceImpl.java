package com.finpro.roomio_backend.orders.service.impl;

import com.finpro.roomio_backend.image.service.CloudinaryService;
import com.finpro.roomio_backend.orders.dto.CreateOrderDTO;
import com.finpro.roomio_backend.orders.dto.OrderDetailsDTO;
import com.finpro.roomio_backend.orders.entity.Orders;
import com.finpro.roomio_backend.orders.repository.OrderRepository;
import com.finpro.roomio_backend.orders.service.OrderService;
import com.finpro.roomio_backend.users.repository.UsersRepository;
import com.finpro.roomio_backend.utils.SignatureVerifier;
import com.midtrans.Config;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransSnapApi;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UsersRepository usersRepository;
    private final CloudinaryService cloudinaryService;
    private final SignatureVerifier signatureVerifier;
    private final Config midtransConfig;
    private MidtransSnapApi snap;
    @PostConstruct
    public void init() {
        this.snap = new com.midtrans.service.impl.MidtransSnapApiImpl(midtransConfig);
    }

    @Override
    @Transactional
    public OrderDetailsDTO createOrderManualTransfer(CreateOrderDTO createOrderDTO, Authentication authentication) {
        // get user
        String user = authentication.getName();
        Long userId = usersRepository.findByEmail(user).get().getId();

        // create order with status waiting (1)
        Orders order = new Orders();
        order.setUserId(userId);
//        order.setRoomId(createOrderDTO.getRoomId());
        order.setCheckin(createOrderDTO.getCheckin());
        order.setCheckout(createOrderDTO.getCheckout());
        order.setNumberOfPeople(createOrderDTO.getNumberOfPeople());
        order.setTotalPrice(createOrderDTO.getTotalPrice());
        order.setPriceExcludeFees(createOrderDTO.getPriceExcludeFees());
        order.setQty(createOrderDTO.getQty());
        order.setPaymentType(createOrderDTO.getPaymentType());
        order.setStatusId(1);

        order = orderRepository.save(order);

        // Update room availability for the selected dates
        //updateRoomAvailability(createOrderDTO.getRoomId(), createOrderDTO.getCheckin(), createOrderDTO.getCheckout(), createOrderDTO.getQty());


        return convertToOrderDetailsDto(order);

    }

    @Override
    @Transactional
    public OrderDetailsDTO uploadPaymentProof(Authentication authentication, Long orderId, MultipartFile file) throws IOException {
        // get the user
        String user = authentication.getName();
        Long userId = usersRepository.findByEmail(user).get().getId();

        // get the order
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found."));

        validateUserandOrder(userId, order);
        validateImage(file);
        String imageUrl = cloudinaryService.uploadFile(file, "payment-proof");
        order.setPaymentProof(imageUrl);
        // update order status to "pending"
        order.setStatusId(2);

        orderRepository.save(order);

        return convertToOrderDetailsDto(order);

    }

    @Override
    @Transactional
    public String createOrderMidtrans(CreateOrderDTO createOrderDTO, Authentication authentication) throws MidtransError {
        // get user
        String user = authentication.getName();
        Long userId = usersRepository.findByEmail(user).get().getId();

        // create order with status waiting (1)
        Orders order = new Orders();
        order.setUserId(userId);
//        order.setRoomId(createOrderDTO.getRoomId());
        order.setCheckin(createOrderDTO.getCheckin());
        order.setCheckout(createOrderDTO.getCheckout());
        order.setNumberOfPeople(createOrderDTO.getNumberOfPeople());
        order.setTotalPrice(createOrderDTO.getTotalPrice());
        order.setPriceExcludeFees(createOrderDTO.getPriceExcludeFees());
        order.setQty(createOrderDTO.getQty());
        order.setPaymentType(createOrderDTO.getPaymentType());
        order.setStatusId(1);

        order = orderRepository.save(order);

        // send order details to midtrans to get snap token
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", order.getId().toString());
        transactionDetails.put("gross_amount", order.getTotalPrice().intValue());
        params.put("transaction_details", transactionDetails);
        String snapToken = snap.createTransactionToken(params);

//        log.info("order id: " + order.getId());
//        log.info("snapToken: " + order.getId());

        return snapToken;
    }

    @Override
    public String handleMidtransNotification(Map<String, Object> notificationData) {
        try {
            // Extract data from notification
            String orderId = (String) notificationData.get("order_id");
            String statusCode = (String) notificationData.get("status_code");
            String grossAmount = (String) notificationData.get("gross_amount");
            String receivedSignature = (String) notificationData.get("signature_key");

            // Check if all required fields are present
            if (orderId == null || statusCode == null || grossAmount == null || receivedSignature == null) {
                throw new RuntimeException("Missing required fields in the notification.");
            }

            // Generate signature key to verify
            String generatedSignature = signatureVerifier.generateSignature(orderId, statusCode, grossAmount);

            // Verify the signature key
            if (!generatedSignature.equals(receivedSignature)) {
                throw new RuntimeException("Invalid signature");
            }

            // Process the valid notification
            String transactionStatus = (String) notificationData.get("transaction_status");
            String fraudStatus = (String) notificationData.get("fraud_status");
            Orders order = orderRepository.findById(Long.parseLong(orderId)).orElseThrow(() -> new RuntimeException("Order not found"));

            if ("capture".equals(transactionStatus)) {
                if ("accept".equals(fraudStatus)) {
                    order.setStatusId(3); // Status 3: "confirmed"
                } else if ("deny".equals(fraudStatus)) {
                    order.setStatusId(4); // Status 4: "canceled"
                }
            } else if ("settlement".equals(transactionStatus)) {
                order.setStatusId(3); // Status 3: "confirmed"
            } else if ("cancel".equals(transactionStatus) || "expire".equals(transactionStatus)) {
                order.setStatusId(4); // Status 4: "canceled"
            } else if ("pending".equals(transactionStatus)) {
                order.setStatusId(1); // Status 1: "waiting"
            }

            // Save the updated order
            orderRepository.save(order);

            return "Notification received and processed";

        } catch (Exception e) {
            // Log error and rethrow
            System.err.println("Error processing notification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public String userCancelOrder(Authentication authentication, Long orderId) {
        // get the user
        String user = authentication.getName();
        Long userId = usersRepository.findByEmail(user).get().getId();

        // get the order
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found."));

        // check if the order belongs to the user
        if (order.getUserId() != userId) {
            throw new RuntimeException("The order does not belong to this user.");
        }

        // check the order status
        if (order.getStatusId() != 1) {
            throw new RuntimeException("Only unpaid order can be canceled.");
        }

        order.setStatusId(4);

        return "Order canceled successfully";
    }

    @Override
    public Page<Orders> userGetOrders(Authentication authentication,
                                      Integer statusId,
                                      Optional<Long> orderId,
                                      Optional<LocalDate> startDate,
                                      Optional<LocalDate> endDate,
                                      int page,
                                      int size) {
        // get the user
        String user = authentication.getName();
        Long userId = usersRepository.findByEmail(user).get().getId();

        PageRequest pageable = PageRequest.of(page, size);

        // Retrieve orders with filters
        return orderRepository.findOrdersByStatusAndFilter(
                userId,
                statusId,
                orderId.orElse(null),
                startDate.orElse(null),
                endDate.orElse(null),
                pageable);
    }


//    @Override
//    public String handleMidtransNotification(Map<String, Object> notificationData) {
//        // extract data from notification
//        String orderId = (String) notificationData.get("order_id");
//        String statusCode = (String) notificationData.get("status_code");
//        String grossAmount = (String) notificationData.get("gross_amount");
//        String receivedSignature = (String) notificationData.get("signature_key");
//
//        // generate signature key to verify
//        String generatedSignature = signatureVerifier.generateSignature(orderId, statusCode, grossAmount);
//
//        // verify the signature key
//        if (!generatedSignature.equals(receivedSignature)) {
//            throw new RuntimeException("Invalid signature");
//        }
//
//        // process the valid notification
//        String transactionStatus = (String) notificationData.get("transaction_status");
//        String fraudStatus = (String) notificationData.get("fraud_status");
//        Orders order = orderRepository.findById(Long.parseLong(orderId)).orElseThrow(() -> new RuntimeException("Order not found"));
//
//        if ("capture".equals(transactionStatus)) {
//            if ("accept".equals(fraudStatus)) {
//                order.setStatusId(3);
//            } else if ("deny".equals(fraudStatus)) {
//                order.setStatusId(4);
//            }
//        } else if ("settlement".equals(transactionStatus)) {
//            order.setStatusId(3);
//        } else if ("cancel".equals(transactionStatus) || "expire".equals(transactionStatus)) {
//            order.setStatusId(4);
//        } else if ("pending".equals(transactionStatus)) {
//            order.setStatusId(2);
//        }
//
//        orderRepository.save(order);
//
//        return "Notification received and processed";
//    }

    private void validateUserandOrder(Long userId, Orders order) {
        // check if the order belongs to the user
        if (order.getUserId() != userId) {
            throw new RuntimeException("The order does not belong to this user.");
        }

        // make sure the payment type is manual transfer
        if (!order.getPaymentType().equals("Manual Transfer")) {
            throw new RuntimeException("Uploading payment proof is only for orders with manual transfer");
        }

        // make sure the order status is waiting (1)
        if (order.getStatusId() != 1) {
            throw new RuntimeException("Uploading payment proof is only for orders with status waiting");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()){
            throw new RuntimeException("Please upload an image");
        }

        if (file.getSize() > 1_000_000) {
            throw new RuntimeException("Image size must not exceed 1MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList("image/jpg", "image/jpeg", "image/png").contains(contentType)) {
            throw new RuntimeException("Invalid image format. Allowed formats: .jpg and .png");
        }
    }

    private OrderDetailsDTO convertToOrderDetailsDto(Orders order) {
        return new OrderDetailsDTO(
        order.getId(),
        order.getCheckin(),
        order.getCheckout(),
        order.getNumberOfPeople(),
        order.getQty(),
        order.getTotalPrice(),
        order.getPriceExcludeFees(),
        order.getPaymentType(),
        order.getStatusId(),
                order.getPaymentProof()
        );
    }

//    private void updateRoomAvailability(Long roomId, LocalDate checkin, LocalDate checkout, int qtyOrdered) {
//        // Loop through each date between check-in and check-out
//        for (LocalDate date = checkin; !date.isAfter(checkout); date = date.plusDays(1)) {
//            Optional<RoomAvailability> existingAvailability = roomAvailabilityRepository.findByRoomIdAndDate(roomId, date);
//
//            if (existingAvailability.isPresent()) {
//                // Update existing record
//                RoomAvailability availability = existingAvailability.get();
//                int updatedQtyLeft = availability.getQtyLeft() - qtyOrdered;
//                if (updatedQtyLeft < 0) {
//                    throw new IllegalStateException("Not enough room availability for date: " + date);
//                }
//                availability.setQtyLeft(updatedQtyLeft);
//                roomAvailabilityRepository.save(availability);
//            } else {
//                // Fetch the total room quantity from the room table
//                int totalRoomQty = roomsRepository.findById(roomId)
//                        .orElseThrow(() -> new IllegalStateException("Room not found"))
//                        .getQty();
//
//                // Create new room availability record
//                RoomAvailability newAvailability = new RoomAvailability();
//                newAvailability.setRoomId(roomId);
//                newAvailability.setDate(date);
//                newAvailability.setQtyLeft(totalRoomQty - qtyOrdered);
//                roomAvailabilityRepository.save(newAvailability);
//            }
//        }
//    }
}
