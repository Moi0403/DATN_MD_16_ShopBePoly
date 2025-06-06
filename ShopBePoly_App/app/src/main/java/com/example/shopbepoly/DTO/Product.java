package com.example.shopbepoly.DTO;

import java.util.List;

public class Product {
    private String _id;
    private String nameproduct;
    private Category id_category;  // đổi từ String thành Category
    private int price;
    private int quantity;
    private String description;
    private String avt_imgproduct;
    private List<String> list_imgproduct;
    private int size;
    private String color;
    private int stock;
    private int sold;

    public Product() {
    }

    public Product(String _id, String nameproduct, Category id_category, int price, int quantity, String description, String avt_imgproduct, List<String> list_imgproduct, int size, String color, int stock, int sold) {
        this._id = _id;
        this.nameproduct = nameproduct;
        this.id_category = id_category;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.avt_imgproduct = avt_imgproduct;
        this.list_imgproduct = list_imgproduct;
        this.size = size;
        this.color = color;
        this.stock = stock;
        this.sold = sold;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getNameproduct() {
        return nameproduct;
    }

    public void setNameproduct(String nameproduct) {
        this.nameproduct = nameproduct;
    }

    public Category getId_category() {
        return id_category;
    }

    public void setId_category(Category id_category) {
        this.id_category = id_category;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvt_imgproduct() {
        return avt_imgproduct;
    }

    public void setAvt_imgproduct(String avt_imgproduct) {
        this.avt_imgproduct = avt_imgproduct;
    }

    public List<String> getList_imgproduct() {
        return list_imgproduct;
    }

    public void setList_imgproduct(List<String> list_imgproduct) {
        this.list_imgproduct = list_imgproduct;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public String getImageUrl() {
        if (avt_imgproduct != null && !avt_imgproduct.isEmpty()) {
            return "http://192.168.1.3:3000/uploads/" + avt_imgproduct;
        }
        return "";
    }

    public String getFormattedPrice() {
        return String.format("%,d₫", price);
    }
}
