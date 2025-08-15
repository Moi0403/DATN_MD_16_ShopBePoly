package com.example.shopbepoly.DTO;

import java.util.List;
import java.util.Objects;

public class ListReview {
    private String _id;
    private User userId;   // user (server populate)
    private String productId;
    private String orderId;
    private int rating;
    private String comment;
    private List<String> images;
    private String createdAt;

    // ---- Getter/Setter ----
    public String getId() {  // ✅ thay get_id() -> getId()
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public String getUser() {   // ✅ tiện cho so sánh currentUserId
        return userId != null ? userId.getId() : null;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // ---- equals & hashCode để merge không trùng _id ----
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListReview)) return false;
        ListReview that = (ListReview) o;
        return _id != null && _id.equals(that._id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id);
    }
}