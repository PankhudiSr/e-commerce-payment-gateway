package com.intern.ecommerce.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.intern.ecommerce.paymentgateway.model.Orders;

import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Integer> {

    Orders findByRazorpayOrderId(String razorpayId);

    List<Orders> findAllByRazorpayOrderId(String razorpayId);

    List<Orders> findByUserId(Integer userId);
}