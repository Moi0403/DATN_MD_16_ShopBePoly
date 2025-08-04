package com.example.shopbepoly.DTO;

import com.google.gson.annotations.SerializedName;

public class Banner {
    @SerializedName("_id") // Đổi tên id để khớp với server MongoDB
    private String id;
    private String name; // Thêm trường name
    private String imageUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}