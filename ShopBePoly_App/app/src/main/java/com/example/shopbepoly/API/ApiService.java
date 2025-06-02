package com.example.shopbepoly.API;

import android.util.Log;
import com.example.shopbepoly.DTO.Order;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    static final String TAG = "ApiService";

    @GET("api/list_order")
    Call<List<Order>> getOrderList();

    @GET("api/list_order/{id}")
    Call<Order> getOrderDetail(@Path("id") String id);

    @POST("api/order")
    Call<Order> addOrder(@Body Order order);

    @DELETE("api/order/{id}")
    Call<Void> deleteOrder(@Path("id") String id);

}

