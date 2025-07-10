package com.example.shopbepoly.DTO;

public class AdminResponse {
    private String _id;
    private String username;
    private String password;
    private String name;
    private String email;
    private long phone_number;
    private String avt_user;
    private int role;
    private int __v;

    public AdminResponse() {
    }

    public AdminResponse(String _id, String username, String password, String name, String email, long phone_number, String avt_user, int role, int __v) {
        this._id = _id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone_number = phone_number;
        this.avt_user = avt_user;
        this.role = role;
        this.__v = __v;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(long phone_number) {
        this.phone_number = phone_number;
    }

    public String getAvt_user() {
        return avt_user;
    }

    public void setAvt_user(String avt_user) {
        this.avt_user = avt_user;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int get__v() {
        return __v;
    }

    public void set__v(int __v) {
        this.__v = __v;
    }
}
