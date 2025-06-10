package com.example.shopbepoly.API;

import android.util.Log;

import com.example.shopbepoly.DTO.Category;
import com.example.shopbepoly.DTO.LoginRequest;
import com.example.shopbepoly.DTO.LoginResponse;
import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    static final String TAG = "ApiService";

    @GET("api/list_order")
    Call<List<Order>> getOrderList();

    @DELETE("api/order/{id}")
    Call<Void> deleteOrder(@Path("id") String id);
    // Product
    @GET("list_product")
    Call<List<Product>> getProducts();

    @GET("products_by_category/{categoryId}")
    Call<List<Product>> getProductsByCategory(@Path("categoryId") String categoryId);

    @GET("search_product")
    Call<List<Product>> searchProduct(@Query("q") String keyword);

    @GET("list_category")
    Call<List<Category>> getCategories();

    @POST("register")
    Call<Void> register(@Body User user);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

}

