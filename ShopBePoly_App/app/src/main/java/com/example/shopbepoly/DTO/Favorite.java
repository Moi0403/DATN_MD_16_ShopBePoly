package com.example.shopbepoly.DTO;

public class Favorite {
    private String _id;
    private String id_user;
    private String id_product; // giữ nguyên là Product

    public Favorite() {
    }

    public Favorite(String id_user, String id_product) {
        this.id_user = id_user;
        this.id_product = id_product; // ✅ truyền vào object Product
    }

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }

    public String getId_user() { return id_user; }
    public void setId_user(String id_user) { this.id_user = id_user; }

    public String getId_product() {
        return id_product;
    }

    public void setId_product(String id_product) {
        this.id_product = id_product;
    }
}
