package com.intern.ecommerce.paymentgateway.service;

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.intern.ecommerce.paymentgateway.model.Orders;
import com.intern.ecommerce.paymentgateway.repository.OrdersRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.annotation.PostConstruct;

@Service
public class OrderService {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrdersRepository ordersRepository;

    @Value("${razorpay.key.id}")
    private String razorpayId;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    private RazorpayClient razorpayClient;

    // Initialize Razorpay client
    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing Razorpay client");
            this.razorpayClient = new RazorpayClient(razorpayId, razorpaySecret);
        } catch (RazorpayException e) {
            logger.error("Failed to initialize Razorpay client", e);
            throw new RuntimeException("Razorpay initialization failed");
        }
    }

    // Create Order
    public Orders createOrder(Orders order) {

        try {
            logger.info("Creating order for email: {}", order.getEmail());

            JSONObject options = new JSONObject();
            options.put("amount", order.getAmount() * 100); // in paise
            options.put("currency", "INR");
            options.put("receipt", order.getEmail());

            Order razorpayOrder = razorpayClient.orders.create(options);

            order.setRazorpayOrderId(razorpayOrder.get("id"));
            order.setOrderStatus(razorpayOrder.get("status"));

            logger.info("Razorpay order created with ID: {}", razorpayOrder.get("id").toString());

            Orders savedOrder = ordersRepository.save(order);
            logger.info("Order saved in database with ID: {}", savedOrder.getOrderId());

            return savedOrder;

        } catch (RazorpayException e) {
            logger.error("Error while creating Razorpay order", e);
            throw new RuntimeException("Payment gateway error. Please try again.");
        } catch (Exception e) {
            logger.error("Unexpected error while creating order", e);
            throw new RuntimeException("Order creation failed");
        }
    }

    // Update payment status
    public Orders updateStatus(Map<String, String> map) {

        logger.info("Updating payment status");

        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("Callback data is missing");
        }

        String razorpayOrderId = map.get("razorpay_order_id");

        if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
            throw new IllegalArgumentException("Razorpay order ID is missing");
        }

        Orders order = ordersRepository.findByRazorpayOrderId(razorpayOrderId);

        if (order == null) {
            logger.error("Order not found for Razorpay Order ID: {}", razorpayOrderId);
            throw new RuntimeException("Order not found");
        }

        order.setOrderStatus("PAYMENT DONE");

        Orders updatedOrder = ordersRepository.save(order);

        logger.info("Payment completed for Order ID: {}", updatedOrder.getOrderId());

        return updatedOrder;
    }
}

