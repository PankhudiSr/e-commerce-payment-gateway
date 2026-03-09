package com.intern.ecommerce.paymentgateway.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrderResponse {
    private Integer orderId;
    private Integer quantity;
    private String orderStatus;
    private String paymentMode;
    private LocalDateTime createdAt;
    private LocalDate estimatedDeliveryDate;

    private Integer userId;
    private String name;
    private String email;
    private Integer amount;

    // Product info
    private Long productId;
    private String productName;
    private String productImage;

    // Vendor info
    private Long vendorId;

    public Long getVendorId() {return vendorId;}
    public void setVendorId(Long vendorId) {this.vendorId = vendorId;}

    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}

    public String getOrderStatus() {return orderStatus;}
    public void setOrderStatus(String orderStatus) {this.orderStatus = orderStatus;}

    public void setOrderId(Integer orderId) {this.orderId = orderId;}
    public Integer getOrderId() {return orderId;}

    public String getProductImage() {return productImage;}
    public void setProductImage(String productImage) {this.productImage = productImage;}

    public String getProductName() {return productName;}
    public void setProductName(String productName) {this.productName = productName;}

    public Long getProductId() {return productId;}
    public void setProductId(Long productId) {this.productId = productId;}

    public Integer getAmount() {return amount;}
    public void setAmount(Integer amount) {this.amount = amount;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public Integer getUserId() {return userId;}
    public void setUserId(Integer userId) {this.userId = userId;}

    public LocalDate getEstimatedDeliveryDate() {return estimatedDeliveryDate;}
    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {this.estimatedDeliveryDate = estimatedDeliveryDate;}

    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public String getPaymentMode() {return paymentMode;}
    public void setPaymentMode(String paymentMode) {this.paymentMode = paymentMode;}
}