package com.example.shopbepoly.DTO;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

public class ListReview {
    @SerializedName("_id")
    private String id;

    @SerializedName("user")
    private User user;  // server trả về "user": { _id, name, avt_user }

    @SerializedName("productId")
    private String productId;

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("images")
    private List<String> images;

    @SerializedName("createdAt")
    private String createdAt;

    // ---- Getter & Setter ----
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // tiện cho so sánh currentUserId
    public String getUserId() {
        return user != null ? user.getId() : null;
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

    // ---- Helper để lấy tên & avatar ----
    public String getDisplayName() {
        return user != null && user.getName() != null
                ? user.getName()
                : "Người dùng";
    }

    public String getAvatar() {
        return user != null && user.getAvatar() != null
                ? user.getAvatar()
                : "https://default-avatar.png";
    }

    // ---- equals & hashCode dựa vào id ----
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListReview)) return false;
        ListReview that = (ListReview) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ---- toString để debug ----
    @Override
    public String toString() {
        return "ListReview{" +
                "id='" + id + '\'' +
                ", user=" + (user != null ? user.getId() : "null") +
                ", productId='" + productId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", images=" + images +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}