package com.example.shopbepoly.DTO;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Cart implements Serializable {
    private String _id;
    @SerializedName("id_user")
    private String idUser;
    @SerializedName("id_product")
    private Product idProduct;
    @SerializedName("img_cart")
    private String img_cart;
    private int quantity;
    private int price;
    @SerializedName("size")
    private int size;
    private int total;
    private int status;
    @SerializedName("color")
    private String color;
    private boolean checked;

    public Cart() {
    }


    public Cart(String _id, String idUser, Product idProduct, int quantity, int price, int size, int total, int status) {
        this._id = _id;
        this.idUser = idUser;
        this.idProduct = idProduct;
        this.quantity = quantity;
        this.price = price;
        this.size = size;
        this.total = total;
        this.status = status;
    }

    public String getImg_cart() {
        return img_cart;
    }

    public void setImg_cart(String img_cart) {
        this.img_cart = img_cart;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public Product getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Product idProduct) {
        this.idProduct = idProduct;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
