package com.intern.ecommerce.paymentgateway.service;
import com.intern.ecommerce.paymentgateway.security.AESUtil;
import com.intern.ecommerce.paymentgateway.common.Constants;

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
import com.razorpay.Payment;


import jakarta.annotation.PostConstruct;

@Service
public class OrderService {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrdersRepository ordersRepository;

    @Value("${razorpay.key.id.enc}")
    private String encryptedKeyId;

    @Value("${razorpay.key.secret.enc}")
    private String encryptedKeySecret;



    private RazorpayClient razorpayClient;

    // Initialize Razorpay client
    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing Razorpay client");

            String razorpayId = AESUtil.decrypt(encryptedKeyId);
            String razorpaySecret = AESUtil.decrypt(encryptedKeySecret);

            this.razorpayClient = new RazorpayClient(razorpayId, razorpaySecret);

            logger.info("Razorpay client initialized successfully");

        } catch (Exception e) {
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
            options.put("currency", Constants.CURRENCY_INR);
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
    // Update payment status (callback_url only)
    public Orders updateStatus(Map<String, String> map) {

        logger.info("Updating payment status");

        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("Callback data is missing");
        }

        String razorpayOrderId = map.get("razorpay_order_id");
        String paymentId = map.get("razorpay_payment_id");

        // ✅ If order_id missing, fetch it from Razorpay using payment_id
        if (razorpayOrderId == null || razorpayOrderId.isBlank()) {

            if (paymentId == null || paymentId.isBlank()) {
                throw new IllegalArgumentException("Both razorpay_order_id and razorpay_payment_id are missing");
            }

            try {
                Payment payment = razorpayClient.payments.fetch(paymentId);
                Object oid = payment.get("order_id");
                razorpayOrderId = (oid != null) ? oid.toString() : null;

                logger.info("Fetched order_id from payment: {}", razorpayOrderId);

            } catch (RazorpayException e) {
                logger.error("Failed to fetch payment details from Razorpay", e);
                throw new RuntimeException("Failed to fetch payment details");
            }
        }

        if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
            throw new IllegalArgumentException("Razorpay order ID is missing even after fetching payment");
        }

        Orders order = ordersRepository.findByRazorpayOrderId(razorpayOrderId);

        if (order == null) {
            logger.error("Order not found for Razorpay Order ID: {}", razorpayOrderId);
            throw new RuntimeException("Order not found");
        }

        order.setOrderStatus(Constants.PAYMENT_DONE);

        Orders updatedOrder = ordersRepository.save(order);
        logger.info("Payment completed for Order ID: {}", updatedOrder.getOrderId());

        return updatedOrder;
    }

}