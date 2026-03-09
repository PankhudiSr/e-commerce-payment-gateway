package com.intern.ecommerce.paymentgateway.service;

import com.intern.ecommerce.paymentgateway.security.AESUtil;
import com.intern.ecommerce.paymentgateway.common.Constants;
import com.intern.ecommerce.paymentgateway.dto.ProductDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
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

    public List<Orders> getOrdersByUserId(Integer userId) {
        return ordersRepository.findByUserId(userId);
    }

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

    // ✅ ONLINE: Create Razorpay order + save in DB
    public Orders createOrder(Orders order) {

        try {
            logger.info("Creating ONLINE order for email: {}", order.getEmail());

            // Ensure ONLINE fields
            order.setPaymentMode("ONLINE");
            order.setOrderStatus("PLACED");

            // Estimated delivery: ONLINE -> 4 days
            if (order.getEstimatedDeliveryDate() == null) {
                order.setEstimatedDeliveryDate(LocalDate.now().plusDays(4));
            }

            RestTemplate restTemplate = new RestTemplate();

            String productUrl = "http://localhost:8080/api/product/" + order.getProductId();

            ResponseEntity<ProductDTO> response =
                    restTemplate.getForEntity(productUrl, ProductDTO.class);

            ProductDTO product = response.getBody();

            if (product == null || product.getVendor() == null) {
                throw new RuntimeException("Vendor not found for product " + order.getProductId());
            }


            Long vendorId = product.getVendor().getId();

            order.setVendorId(vendorId);


            JSONObject options = new JSONObject();
            options.put("amount", order.getAmount() * 100); // paise
            options.put("currency", Constants.CURRENCY_INR);
            options.put("receipt", order.getEmail());

            Order razorpayOrder = razorpayClient.orders.create(options);

            order.setRazorpayOrderId(razorpayOrder.get("id").toString());

            logger.info("Razorpay order created with ID: {}", order.getRazorpayOrderId());

            Orders savedOrder = ordersRepository.save(order);
            logger.info("Order saved in database with ID: {}", savedOrder.getOrderId());



            String deliveryUrl = "http://localhost:8080/api/delivery/create"
                    + "?productId=" + order.getProductId()
                    + "&vendorId=" + order.getVendorId()
                    + "&userId=" + order.getUserId()
                    + "&quantity=" + order.getQuantity();

            restTemplate.postForObject(deliveryUrl, null, String.class);

            return savedOrder;

        } catch (RazorpayException e) {
            logger.error("Error while creating Razorpay order", e);
            throw new RuntimeException("Payment gateway error. Please try again.");
        } catch (Exception e) {
            logger.error("Unexpected error while creating order", e);
            throw new RuntimeException("Order creation failed");
        }
    }

    // ✅ COD: Create order directly (no Razorpay)
    public Orders createCodOrder(Orders order) {
        try {
            logger.info("Creating COD order for email: {}", order.getEmail());

            order.setPaymentMode("COD");
            order.setOrderStatus("PLACED");
            order.setRazorpayOrderId(null);
            // Estimated delivery: COD -> 5 days
            if (order.getEstimatedDeliveryDate() == null) {
                order.setEstimatedDeliveryDate(LocalDate.now().plusDays(5));
            }

            RestTemplate restTemplate = new RestTemplate();

            String productUrl = "http://localhost:8080/api/product/" + order.getProductId();

            ResponseEntity<ProductDTO> response =
                    restTemplate.getForEntity(productUrl, ProductDTO.class);

            ProductDTO product = response.getBody();

            if (product == null || product.getVendor() == null) {
                throw new RuntimeException("Vendor not found for product " + order.getProductId());
            }


            Long vendorId = product.getVendor().getId();

            order.setVendorId(vendorId);

            Orders saved = ordersRepository.save(order);
            logger.info("COD Order saved in database with ID: {}", saved.getOrderId());

            return saved;

        } catch (Exception e) {
            logger.error("Unexpected error while creating COD order", e);
            throw new RuntimeException("COD Order creation failed");
        }
    }

    // ✅ Fetch order by id (for Order Success page)
    public Orders getOrderById(Integer orderId) {
        return ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    // ✅ Update payment status (callback_url only)
    public Orders updateStatus(Map<String, String> map) {

        logger.info("Updating payment status");

        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("Callback data is missing");
        }

        String razorpayOrderId = map.get("razorpay_order_id");
        String paymentId = map.get("razorpay_payment_id");

        // If order_id missing, fetch it from Razorpay using payment_id
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

        // Mark as PAID
        order.setOrderStatus("PLACED"); // e.g. "PAID"

        Orders updatedOrder = ordersRepository.save(order);
        logger.info("Payment completed for Order ID: {}", updatedOrder.getOrderId());

        return updatedOrder;
    }



}