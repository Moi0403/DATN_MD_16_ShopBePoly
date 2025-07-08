package com.example.shopbepoly.DTO;

public class Order {
    private String _id;
    private String id_user;
    private String id_product;
    private String img_oder;
    private int quantity;
    private String color;
    private double price;
    private String total;
    private String date;
    private String status;
    private String address;
    private String nameproduct;
    private String pay;

    public Order() {}

    public Order(String id_user, String id_product, String img_oder, int quantity, String color,
                 double price, String total, String date, String status,
                 String address, String nameproduct, String pay) {
        this.id_user = id_user;
        this.id_product = id_product;
        this.img_oder = img_oder;
        this.quantity = quantity;
        this.color = color;
        this.price = price;
        this.total = total;
        this.date = date;
        this.status = status;
        this.address = address;
        this.nameproduct = nameproduct;
        this.pay = pay;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getId_product() {
        return id_product;
    }

    public void setId_product(String id_product) {
        this.id_product = id_product;
    }

    public String getImg_oder() {
        return img_oder;
    }

    public void setImg_oder(String img_oder) {
        this.img_oder = img_oder;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotal() {
        try {
            return Double.parseDouble(total);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNameproduct() {
        return nameproduct;
    }

    public void setNameproduct(String nameproduct) {
        this.nameproduct = nameproduct;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }

    public boolean isCancellable() {
        if (status == null) return true;
        String s = status.toLowerCase();
        return !s.contains("đã hủy") && !s.contains("đã giao") && !s.contains("hoàn thành");
    }
}