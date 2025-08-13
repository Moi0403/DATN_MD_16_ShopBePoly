package com.example.shopbepoly.DTO;

import com.google.gson.annotations.SerializedName;

public class SendMessageResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Message data;

    public String getMessage() {
        return message;
    }

    public Message getData() {
        return data;
    }
}
