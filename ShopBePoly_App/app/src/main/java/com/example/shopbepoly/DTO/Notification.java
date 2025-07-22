package com.example.shopbepoly.DTO;

import java.io.Serializable;
import java.util.List;

public class Notification implements Serializable {
    private String _id;
    private String userId;
    private String title;
    private String content;
    private String createdAt;
    private boolean isRead;
    private String type;
    private List<ProductInfo> products;

    public Notification() {
    }

    public Notification(String _id, String userId, String title, String content, String createdAt,
                        boolean isRead, String type, List<ProductInfo> products) {
        this._id = _id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.type = type;
        this.products = products;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ProductInfo> getProducts() {
        return products;
    }

    public void setProducts(List<ProductInfo> products) {
        this.products = products;
    }

    public static class ProductInfo implements Serializable {
        private String id_product;
        private String productName;
        private String img;

        public ProductInfo() {
        }

        public ProductInfo(String id_product, String productName, String img) {
            this.id_product = id_product;
            this.productName = productName;
            this.img = img;
        }

        public String getId_product() {
            return id_product;
        }

        public void setId_product(String id_product) {
            this.id_product = id_product;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }
    }
}
