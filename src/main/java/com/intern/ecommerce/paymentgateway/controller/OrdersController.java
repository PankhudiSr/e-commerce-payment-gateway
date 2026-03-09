package com.intern.ecommerce.paymentgateway.controller;

import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.intern.ecommerce.paymentgateway.model.Orders;
import com.intern.ecommerce.paymentgateway.service.OrderService;
import com.razorpay.RazorpayException;

@Controller
@CrossOrigin(origins = "http://localhost:5173")
public class OrdersController {

    private static final Logger logger =
            LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private OrderService orderService;



    // Load Orders page (if you are using Thymeleaf / template)
    @GetMapping("/orders")
    public String ordersPage() {
        logger.info("Orders page requested");
        return "orders";
    }

    // ✅ ONLINE: Create Razorpay order
    // Frontend calls: POST /createOrder
    @PostMapping(value = "/createOrder", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Orders> createOrder(@RequestBody Orders orders)
            throws RazorpayException {

        logger.info("Create ONLINE order API called");
        // ensure it's ONLINE
        orders.setPaymentMode("ONLINE");
        // status stays PENDING until paymentCallback updates it
        orders.setOrderStatus("PENDING");

        Orders razorpayOrder = orderService.createOrder(orders);

        logger.info("ONLINE order created successfully with ID: {}", razorpayOrder.getOrderId());
        return new ResponseEntity<>(razorpayOrder, HttpStatus.CREATED);
    }

    // ✅ COD: Create order without Razorpay
    // Frontend calls: POST /createCodOrder
    @PostMapping(value = "/createCodOrder", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Orders> createCodOrder(@RequestBody Orders orders) {

        logger.info("Create COD order API called");
        orders.setPaymentMode("COD");
        orders.setOrderStatus("PENDING");
        orders.setRazorpayOrderId(null);

        Orders saved = orderService.createCodOrder(orders);



//        logger.info("COD order created successfully with ID: {}", saved.getOrderId());
       return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // ✅ Payment callback (Razorpay success/fail)
    @PostMapping("/paymentCallback")
    @ResponseBody
    public ResponseEntity<?> paymentCallback(@RequestParam Map<String, String> response) {

        logger.info("Payment callback received: {}", response);

        Orders updatedOrder = orderService.updateStatus(response);

        return ResponseEntity.ok(Map.of(
                "message", "Payment status updated",
                "orderId", updatedOrder.getOrderId(),
                "razorpayOrderId", updatedOrder.getRazorpayOrderId(),
                "status", updatedOrder.getOrderStatus(),
                "paymentMode", updatedOrder.getPaymentMode(),
                "estimatedDeliveryDate", updatedOrder.getEstimatedDeliveryDate()
        ));
    }

    // ✅ API for Success Page (React)
    // Frontend calls: GET /api/orders/{orderId}
    @GetMapping(value = "/api/orders/{orderId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Orders> getOrder(@PathVariable Integer orderId) {

        logger.info("Get order details for success page: {}", orderId);
        Orders order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/api/orders/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getOrdersByUser(@PathVariable Integer userId) {

        logger.info("Fetching orders for user {}", userId);

        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }
}