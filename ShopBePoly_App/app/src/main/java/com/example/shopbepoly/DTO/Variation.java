package com.example.shopbepoly.DTO;


import java.io.Serializable;
import java.util.List;

public class Variation implements Serializable {
    private int size;
    private int stock;
    private int sold;
    private Color color;
    private String image;
    private List<String> list_imgproduct;
    public static class Color implements Serializable {
        private String name;
        private String code;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public Variation() {
    }

    public Variation(int size, int stock, int sold, Color color, String image, List<String> list_imgproduct) {
        this.size = size;
        this.stock = stock;
        this.sold = sold;
        this.color = color;
        this.image = image;
        this.list_imgproduct = list_imgproduct;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getList_imgproduct() {
        return list_imgproduct;
    }

    public void setList_imgproduct(List<String> list_imgproduct) {
        this.list_imgproduct = list_imgproduct;
    }
}
