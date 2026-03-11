package com.intern.ecommerce.paymentgateway.dto;

import java.util.List;

public class ProductDTO {

    private Long id;
    private String name;

    private VendorDTO vendor;

    private List<ImageDTO> images;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<ImageDTO> getImages() { return images; }
    public void setImages(List<ImageDTO> images) { this.images = images; }

    public VendorDTO getVendor() { return vendor; }
    public void setVendor(VendorDTO vendor) { this.vendor = vendor; }


    // Inner class for images
    public static class ImageDTO {
        private Long id;
        private String imageUrl;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}