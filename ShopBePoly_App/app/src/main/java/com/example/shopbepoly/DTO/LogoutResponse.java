package com.example.shopbepoly.DTO;

import com.google.gson.annotations.SerializedName;

public class LogoutResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
