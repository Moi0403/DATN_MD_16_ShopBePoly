package com.example.shopbepoly.DTO;

import java.io.Serializable;

public class Variation implements Serializable {
    private int size;
    private int stock;
    private int sold;

    public Variation() {}

    public Variation(int size, int stock, int sold) {
        this.size = size;
        this.stock = stock;
        this.sold = sold;
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
}
