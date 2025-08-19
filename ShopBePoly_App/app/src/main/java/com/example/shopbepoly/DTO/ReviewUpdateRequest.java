package com.example.shopbepoly.DTO;

public class ReviewUpdateRequest {
    private int rating;
    private String comment;

    public ReviewUpdateRequest(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}
