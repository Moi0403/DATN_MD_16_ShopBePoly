package com.example.shopbepoly;

import android.os.Bundle;
import android.util.Log;
import com.google.android.material.button.MaterialButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.OrderAdapter;
import com.example.shopbepoly.DTO.Order;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Donhang extends AppCompatActivity {
    private static final String TAG = "Donhang";
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> ordList = new ArrayList<>();
    private ApiService apiService;
    private MaterialButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_donhang);

        try {
            // Initialize views
            recyclerView = findViewById(R.id.listOrd);
            btnBack = findViewById(R.id.btnBack);

            // Setup RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            
            // Initialize API service
            apiService = ApiClient.getApiService();
            if (apiService == null) {
                Log.e(TAG, "API Service is null");
                Toast.makeText(this, "Lỗi khởi tạo kết nối", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Initialize adapter
            adapter = new OrderAdapter(this, ordList, new OrderAdapter.OrderListener() {
                @Override
                public void onDelete(String id) {
                    huyDH(id);
                }
            });
            
            recyclerView.setAdapter(adapter);

            // Setup back button
            btnBack.setOnClickListener(v -> finish());

            // Load orders
            loadord();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadord() {
        Log.d(TAG, "Starting to load orders...");
        try {
            Call<List<Order>> call = apiService.getOrderList();
            if (call == null) {
                Log.e(TAG, "API call is null");
                Toast.makeText(this, "Lỗi kết nối API", Toast.LENGTH_SHORT).show();
                return;
            }

            call.enqueue(new Callback<List<Order>>() {
                @Override
                public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                    Log.d(TAG, "Response received. Code: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        List<Order> orders = response.body();
                        Log.d(TAG, "Number of orders received: " + orders.size());
                        ordList.clear();
                        ordList.addAll(orders);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(Donhang.this, "Đã tải " + orders.size() + " đơn hàng", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorBody = "";
                        try {
                            if (response.errorBody() != null) {
                                errorBody = response.errorBody().string();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                        Log.e(TAG, "Error response: " + response.code() + " - " + errorBody);
                        Toast.makeText(Donhang.this, "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Order>> call, Throwable t) {
                    Log.e(TAG, "Error loading orders", t);
                    String errorMessage = t.getMessage();
                    if (errorMessage == null) {
                        errorMessage = "Không thể kết nối đến server";
                    }
                    Toast.makeText(Donhang.this, "Lỗi kết nối: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadord", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void huyDH(String id) {
        Log.d(TAG, "Cancelling order: " + id);
        try {
            apiService.deleteOrder(id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Order cancelled successfully");
                        loadord();
                        Toast.makeText(Donhang.this, "Đã hủy", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Error cancelling order: " + response.code());
                        Toast.makeText(Donhang.this, "Lỗi hủy: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Error cancelling order", t);
                    Toast.makeText(Donhang.this, "Lỗi hủy: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in huyDH", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
