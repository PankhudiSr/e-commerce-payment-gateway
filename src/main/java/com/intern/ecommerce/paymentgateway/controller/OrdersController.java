
package com.intern.ecommerce.paymentgateway.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intern.ecommerce.paymentgateway.model.Orders;
import com.intern.ecommerce.paymentgateway.service.OrderService;
import com.razorpay.RazorpayException;

@Controller
public class OrdersController {

    private static final Logger logger =
            LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private OrderService orderService;

    // Load Orders page
    @GetMapping("/orders")
    public String ordersPage() {
        logger.info("Orders page requested");
        return "orders";
    }

    // Create Razorpay order
    @PostMapping(value = "/createOrder", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Orders> createOrder(@RequestBody Orders orders)
            throws RazorpayException {

        logger.info("Create order API called");
        Orders razorpayOrder = orderService.createOrder(orders);

        logger.info("Order created successfully with ID: {}", razorpayOrder.getOrderId());
        return new ResponseEntity<>(razorpayOrder, HttpStatus.CREATED);
    }

    // Payment callback
    @PostMapping("/paymentCallback")
    @ResponseBody
    public ResponseEntity<?> paymentCallback(@RequestParam Map<String, String> response) {

        logger.info("Payment callback received: {}", response);

        Orders updatedOrder = orderService.updateStatus(response);

        logger.info("Payment status updated successfully");

        return ResponseEntity.ok(Map.of(
                "message", "Payment status updated",
                "orderId", updatedOrder.getOrderId(),
                "razorpayOrderId", updatedOrder.getRazorpayOrderId(),
                "status", updatedOrder.getOrderStatus()
        ));
    }
}