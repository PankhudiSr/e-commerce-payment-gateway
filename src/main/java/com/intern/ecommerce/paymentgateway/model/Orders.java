package com.intern.ecommerce.paymentgateway.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    private Integer userId;
    private Long productId;
    private Long vendorId;

    private Integer quantity;

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


}