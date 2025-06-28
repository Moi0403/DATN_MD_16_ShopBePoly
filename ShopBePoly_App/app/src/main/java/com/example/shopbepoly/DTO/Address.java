package com.example.shopbepoly.DTO;

import java.io.Serializable;

public class Address implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String address;
    private String label; // Nhà, Công ty, Khác
    private boolean isDefault;

    public Address() {}

    public Address(String id, String name, String phone, String address, String label, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.label = label;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
} 