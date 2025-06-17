package com.example.shopbepoly.DTO;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private String message;
    private User user;

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public static class User {
        @SerializedName("id")
        private String id;

        private String username;
        private String name;
        private int role;

        public String getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getName() {
            return name;
        }

        public int getRole() {
            return role;
        }
    }
}
