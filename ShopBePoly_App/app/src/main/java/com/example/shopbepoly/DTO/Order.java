package com.example.shopbepoly.DTO;

import java.util.List;

public class Order {
    private String _id;
    private String bill;
    private String date;
    private String status;
    private String address;
    private List<String> img;
    private String nameproduct;
    private String pay;

    public Order() {
    }

    public Order(String _id, String bill, String date, String status, String address, List<String> img, String nameproduct, String pay) {
        this._id = _id;
        this.bill = bill;
        this.date = date;
        this.status = status;
        this.address = address;
        this.img = img;
        this.nameproduct = nameproduct;
        this.pay = pay;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getBill() {
        return bill;
    }

    public void setBill(String bill) {
        this.bill = bill;
    }

    public double getBillAsDouble() {
        try {
            String cleanBill = bill.replace("đ", "").replace(".", "");
            return Double.parseDouble(cleanBill);
        } catch (NumberFormatException e) {
            return 0.0;
        }
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

    public List<String> getImg() {
        return img;
    }

    public void setImg(List<String> img) {
        this.img = img;
    }

    public String getFirstImage() {
        if (img != null && !img.isEmpty()) {
            return img.get(0);
        }
        return "";
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

    public boolean isCancellable(){
        if (status == null) return true;
        String statusLower = status.toLowerCase();
        return !statusLower.contains("đã hủy") &&
                !statusLower.contains("đã giao") &&
                !statusLower.contains("hoàn thành");
    }
}


