package com.intern.ecommerce.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.intern.ecommerce.paymentgateway.model.Orders;

public interface OrdersRepository extends JpaRepository<Orders, Integer>{

    Orders findByRazorpayOrderId(String razorpayId);

}
