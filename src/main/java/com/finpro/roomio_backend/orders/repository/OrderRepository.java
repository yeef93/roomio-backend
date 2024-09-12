package com.finpro.roomio_backend.orders.repository;

import com.finpro.roomio_backend.orders.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    @Query("SELECT o FROM Orders o WHERE " +
            "(o.userId = :userId) AND " +
            "(o.statusId = :statusId) AND " +
            "(:orderId IS NULL OR o.id = :orderId) AND " +
            "(:startDate IS NULL OR o.checkin >= :startDate) AND " +
            "(:endDate IS NULL OR o.checkout <= :endDate)")
    Page<Orders> findOrdersByStatusAndFilter(
            Long userId,
            @Param("statusId") Integer statusId,
            @Param("orderId") Long orderId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

}
