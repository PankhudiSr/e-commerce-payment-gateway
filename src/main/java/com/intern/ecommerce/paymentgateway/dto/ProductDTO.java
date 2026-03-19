package com.intern.ecommerce.paymentgateway.dto;

import java.util.List;

public class ProductDTO {

    private Long id;
    private String name;
    private String category;
    private String sizes;
    private Integer quantity;   // ✅ REQUIRED
    private Long discount;
    private Double originalPrice;
    private Double discountPrice;
    private String description;

    private VendorDTO vendor;
    private List<ImageDTO> images;

    // ===== GETTERS & SETTERS =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSizes() { return sizes; }
    public void setSizes(String sizes) { this.sizes = sizes; }

    public Integer getQuantity() { return quantity; }   // ✅ FIX
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Long getDiscount() { return discount; }
    public void setDiscount(Long discount) { this.discount = discount; }

    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }

    public Double getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(Double discountPrice) { this.discountPrice = discountPrice; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public VendorDTO getVendor() { return vendor; }
    public void setVendor(VendorDTO vendor) { this.vendor = vendor; }

    public List<ImageDTO> getImages() { return images; }
    public void setImages(List<ImageDTO> images) { this.images = images; }

    // ===== INNER CLASS FOR IMAGE =====
    public static class ImageDTO {
        private Long id;
        private String imageUrl;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}