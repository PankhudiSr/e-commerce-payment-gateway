package com.intern.ecommerce.paymentgateway.service;

import com.intern.ecommerce.paymentgateway.security.AESUtil;
import com.intern.ecommerce.paymentgateway.common.Constants;
import com.intern.ecommerce.paymentgateway.dto.ProductDTO;
import com.intern.ecommerce.paymentgateway.model.Orders;
import com.intern.ecommerce.paymentgateway.repository.OrdersRepository;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${razorpay.key.id.enc}")
    private String encryptedKeyId;

    @Value("${razorpay.key.secret.enc}")
    private String encryptedKeySecret;

    @Value("${PRODUCT_BACKEND_URL}")
    private String PRODUCT_BACKEND_URL;

    private RazorpayClient razorpayClient;

    public List<Orders> getOrdersByUserId(Integer userId) {
        return ordersRepository.findByUserId(userId);
    }

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

    public Orders createOrder(Orders order) {
        try {
            logger.info("Creating ONLINE order for email: {}", order.getEmail());

            order.setPaymentMode("ONLINE");
            order.setOrderStatus("PLACED");

            if (order.getEstimatedDeliveryDate() == null) {
                order.setEstimatedDeliveryDate(LocalDate.now().plusDays(4));
            }

            ProductDTO product = fetchProductById(order.getProductId());

            if (product == null || product.getVendor() == null) {
                throw new RuntimeException("Vendor not found for product " + order.getProductId());
            }

            Long vendorId = product.getVendor().getId();
            order.setVendorId(vendorId);

            JSONObject options = new JSONObject();
            options.put("amount", order.getAmount() * 100);
            options.put("currency", Constants.CURRENCY_INR);
            options.put("receipt", order.getEmail());

            Order razorpayOrder = razorpayClient.orders.create(options);
            order.setRazorpayOrderId(razorpayOrder.get("id").toString());

            logger.info("Razorpay order created with ID: {}", order.getRazorpayOrderId());

            Orders savedOrder = ordersRepository.save(order);
            logger.info("Order saved in database with ID: {}", savedOrder.getOrderId());

            createDeliveryEntry(savedOrder);

            return savedOrder;

        } catch (RazorpayException e) {
            logger.error("Error while creating Razorpay order", e);
            throw new RuntimeException("Payment gateway error. Please try again.");
        } catch (Exception e) {
            logger.error("Unexpected error while creating order", e);
            throw new RuntimeException("Order creation failed: " + e.getMessage());
        }
    }

    public Orders createCodOrder(Orders order) {
        try {
            logger.info("Creating COD order for email: {}", order.getEmail());

            order.setPaymentMode("COD");
            order.setOrderStatus("PLACED");
            order.setRazorpayOrderId(null);

            if (order.getEstimatedDeliveryDate() == null) {
                order.setEstimatedDeliveryDate(LocalDate.now().plusDays(5));
            }

            ProductDTO product = fetchProductById(order.getProductId());

            if (product == null || product.getVendor() == null) {
                throw new RuntimeException("Vendor not found for product " + order.getProductId());
            }


            Long vendorId = product.getVendor().getId();
            order.setVendorId(vendorId);

            if (product.getQuantity() == null || order.getQuantity() == null) {
                throw new RuntimeException("Product quantity data missing");
            }

            if (order.getQuantity() > product.getQuantity()) {
                throw new RuntimeException("Not enough stock available");
            }

            Orders saved = ordersRepository.save(order);
            logger.info("COD Order saved in database with ID: {}", saved.getOrderId());

            reduceProductStock(saved.getProductId(), saved.getQuantity());

            createDeliveryEntry(saved);

            return saved;

        } catch (Exception e) {
            logger.error("Unexpected error while creating COD order", e);
            throw new RuntimeException("COD Order creation failed: " + e.getMessage());
        }
    }

    private ProductDTO fetchProductById(Long productId) {
        try {
            String productUrl = PRODUCT_BACKEND_URL + "/api/product/" + productId;

            ResponseEntity<ProductDTO> response =
                    restTemplate.getForEntity(productUrl, ProductDTO.class);

            return response.getBody();
        } catch (Exception e) {
            logger.error("Failed to fetch product details for productId={}", productId, e);
            throw new RuntimeException("Failed to fetch product details: " + e.getMessage());
        }
    }

    private void reduceProductStock(Long productId, Integer orderedQty) {
        try {
            if (productId == null || orderedQty == null || orderedQty <= 0) {
                throw new RuntimeException("Invalid productId or ordered quantity");
            }

            String url = PRODUCT_BACKEND_URL
                    + "/api/product/reduce-stock/" + productId
                    + "?quantity=" + orderedQty;

            logger.info("Calling stock reduction API: {}", url);

            restTemplate.postForObject(url, null, Object.class);

            logger.info("Stock reduced successfully for productId={}, qty={}", productId, orderedQty);

        } catch (Exception e) {
            logger.error("Failed to reduce stock for productId={}, qty={}", productId, orderedQty, e);
            throw new RuntimeException("Failed to reduce product stock: " + e.getMessage());
        }
    }

    private void createDeliveryEntry(Orders order) {
        try {
            if (order.getProductId() == null ||
                    order.getVendorId() == null ||
                    order.getUserId() == null ||
                    order.getQuantity() == null ||
                    order.getOrderId() == null) {

                logger.warn(
                        "Skipping delivery creation because productId/vendorId/userId/quantity/orderId is null for orderId={}",
                        order.getOrderId()
                );
                return;
            }

            String url = PRODUCT_BACKEND_URL + "/api/delivery/create"
                    + "?productId=" + order.getProductId()
                    + "&vendorId=" + order.getVendorId()
                    + "&userId=" + order.getUserId()
                    + "&quantity=" + order.getQuantity()
                    + "&orderId=" + order.getOrderId();

            logger.info("Calling delivery API: {}", url);

            restTemplate.postForEntity(url, null, String.class);

            logger.info("Delivery entry created successfully for orderId={}", order.getOrderId());

        } catch (Exception e) {
            logger.error("Failed to create delivery entry for orderId={}", order.getOrderId(), e);
        }
    }


    public Orders getOrderById(Integer orderId) {
        return ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public Orders updateStatus(Map<String, String> map) {

        logger.info("Updating payment status");

        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("Callback data is missing");
        }

        String razorpayOrderId = map.get("razorpay_order_id");
        String paymentId = map.get("razorpay_payment_id");

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

        if ("PAYMENT_DONE".equals(order.getPaymentMode())) {
            logger.info("Payment already updated for Order ID: {}", order.getOrderId());
            return order;
        }

        reduceProductStock(order.getProductId(), order.getQuantity());



        order.setOrderStatus(Constants.PAYMENT_DONE);



        Orders updatedOrder = ordersRepository.save(order);
        logger.info("Payment completed for Order ID: {}", updatedOrder.getOrderId());

        return updatedOrder;
    }

    public Orders updateOrderTrackingStatus(Integer orderId, String status) {
        logger.info("Updating tracking status for orderId={} to {}", orderId, status);

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or blank");
        }

        order.setOrderStatus(status);

        Orders updatedOrder = ordersRepository.save(order);
        logger.info("Tracking status updated successfully for orderId={}", updatedOrder.getOrderId());

        return updatedOrder;
    }
}