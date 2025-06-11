package com.example.shopbepoly.DTO;

import java.io.Serializable;

public class Category implements Serializable {
    private String _id;
    private String title;
    private String cateImg;

    public Category() {
    }

    public Category(String _id, String title, String cateImg) {
        this._id = _id;
        this.title = title;
        this.cateImg = cateImg;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCateImg() {
        return cateImg;
    }

    public void setCateImg(String cateImg) {
        this.cateImg = cateImg;
    }
}
