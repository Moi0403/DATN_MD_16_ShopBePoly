package com.example.shopbepoly.DTO;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DeliveringOrdersResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("count")
    private int count;
    
    @SerializedName("orders")
    private List<Order> orders;
    
    @SerializedName("error")
    private String error;

    public DeliveringOrdersResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
