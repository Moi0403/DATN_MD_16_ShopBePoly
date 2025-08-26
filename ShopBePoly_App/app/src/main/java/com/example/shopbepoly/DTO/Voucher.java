package com.example.shopbepoly.DTO;

import com.google.gson.Gson;
import java.util.Date;

public class Voucher {
    private String _id;
    private String code;
    private String description;
    private String discountType; // "percent" or "fixed"
    private double discountValue;
    private double minOrderValue;
    private int usageLimit;
    private int usedCount;
    private Date startDate;
    private Date endDate;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;

    // Constructors
    public Voucher() {}

    public Voucher(String _id, String code, String description, String discountType,
                   double discountValue, double minOrderValue, int usageLimit,
                   int usedCount, Date startDate, Date endDate, boolean isActive) {
        this._id = _id;
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderValue = minOrderValue;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public double getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public boolean isExpired() {
        return new Date().after(endDate);
    }

    public boolean isAvailable() {
        return isActive && !isExpired() && usedCount < usageLimit;
    }

    public int getRemainingUsage() {
        return Math.max(0, usageLimit - usedCount);
    }

    // Convert to JSON string
    public String toJson() {
        return new Gson().toJson(this);
    }

    // Create from JSON string
    public static Voucher fromJson(String json) {
        return new Gson().fromJson(json, Voucher.class);
    }
}