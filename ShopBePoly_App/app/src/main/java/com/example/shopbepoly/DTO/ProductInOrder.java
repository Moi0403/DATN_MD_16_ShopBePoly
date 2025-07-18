package com.example.shopbepoly.DTO;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProductInOrder implements Serializable {
    @SerializedName("id_product")
    private Product id_product;
    private int quantity;
    private String color;
    private String size;
    private int price;
    private String img;

    public ProductInOrder() {
    }


    public Product getId_product() {
        return id_product;
    }

    public void setId_product(Product id_product) {
        this.id_product = id_product;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getImg(String color) {
        if (id_product != null && id_product.getVariations() != null) {
            for (Variation v : id_product.getVariations()) {
                if (v.getColor().getName().equalsIgnoreCase(color)) {
                    return v.getImage();
                }
            }
        }
        return id_product != null ? id_product.getAvt_imgproduct() : null;
    }

}
