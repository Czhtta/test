package com.comp5348.store.repository;

import com.comp5348.store.entity.Order;
import com.comp5348.store.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserId(Long userId);
    
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderStatus = :status")
    List<Order> findByUserIdAndOrderStatus(@Param("userId") Long userId, @Param("status") OrderStatus orderStatus);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate")
    List<Order> findByOrderDateBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                     @Param("endDate") java.time.LocalDateTime endDate);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.orderStatus = :newStatus WHERE o.id = :orderId AND o.orderStatus = :expectedCurrentStatus")
    int updateStatusIfCurrentStatusIs(@Param("orderId") Long orderId,
                                      @Param("newStatus") OrderStatus newStatus,
                                      @Param("expectedCurrentStatus") OrderStatus expectedCurrentStatus);
}
