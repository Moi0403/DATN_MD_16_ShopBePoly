package com.example.shopbepoly.Screen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;


import com.example.shopbepoly.DTO.Notification;
import com.example.shopbepoly.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThongBao extends AppCompatActivity {
    RecyclerView rcNotify;
    private com.example.shopbepoly.adapter.NotificationAdapter adapter;
    List<Notification> list = new  ArrayList<>();
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_bao);

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("userId", null);

        // Ánh xạ RecyclerView và set adapter
        rcNotify = findViewById(R.id.rcNotify);
        adapter = new com.example.shopbepoly.adapter.NotificationAdapter(this, list);
        rcNotify.setLayoutManager(new LinearLayoutManager(this));
        rcNotify.setAdapter(adapter);

        // Gọi API lấy danh sách thông báo
        loadNotifications();
    }

    private void loadNotifications() {
        ApiService apiService = ApiClient.getApiService();
        apiService.getNotifications(userId).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    list.clear();
                    list.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("Notification", "Không có dữ liệu hoặc lỗi phản hồi");
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Log.e("Notification", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
