package com.intern.ecommerce.paymentgateway.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    private String name;
    private String email;

    // amount in rupees
    private Integer amount;

    // ONLINE or COD
    private String paymentMode;

    // PENDING / PAID / FAILED
    private String orderStatus;

    // only for ONLINE
    private String razorpayOrderId;

    // estimated delivery date
    private LocalDate estimatedDeliveryDate;

    // created time
    private LocalDateTime createdAt;

    // ---- added for quick vendor tracking solution ----
    private Long productId;
    private Long vendorId;
    private Integer quantity;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.estimatedDeliveryDate == null) {
            if ("ONLINE".equalsIgnoreCase(this.paymentMode)) {
                this.estimatedDeliveryDate = LocalDate.now().plusDays(4);
            } else {
                this.estimatedDeliveryDate = LocalDate.now().plusDays(5);
            }
        }

        if (this.orderStatus == null || this.orderStatus.trim().isEmpty()) {
            this.orderStatus = "PENDING";
        }
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}