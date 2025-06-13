package com.example.shopbepoly.DTO;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    private String _id;
    private String nameproduct;
    private Category id_category;  // đổi từ String thành Category
    private int price;
    private int quantity;
    private String description;
    private String avt_imgproduct;
    private List<String> list_imgproduct;
    private String color;
    private int sold;
    private List<Variation> variations;

    public Product() {
    }
    public List<Variation> getVariations() {
        return variations;
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
        this.color = color;
        this.sold = sold;

    }
    public void setVariations(List<Variation> variations) {
        this.variations = variations;
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


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }
    public String getFormattedPrice() {
        return String.format("%,d₫", price);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product p = (Product) o;
        return _id != null && _id.equals(p._id);   // so sánh theo id
    }

    @Override
    public int hashCode() {
        return _id != null ? _id.hashCode() : 0;
    }
}
