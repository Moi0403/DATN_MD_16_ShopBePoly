package com.example.shopbepoly.DTO;

import java.util.List;

public class Review {
    private String userId;
    private String productId;
    private String orderId;
    private int rating;
    private String comment;
    private List<String> images; // nếu không có thì gửi mảng rỗng

    public Review(String userId, String productId, String orderId, int rating, String comment, List<String> images) {
        this.userId = userId;
        this.productId = productId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.images = images;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
