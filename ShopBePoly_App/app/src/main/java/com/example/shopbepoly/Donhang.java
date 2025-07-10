package com.example.shopbepoly;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.OrderAdapter;
import com.example.shopbepoly.DTO.Order;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Donhang extends AppCompatActivity {

    private static final String TAG = "Donhang";

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private ApiService apiService;
    private ImageButton btnBack;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_donhang);

        SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        userId = loginPrefs.getString("userId", "");

        if (userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.listOrd);
        btnBack = findViewById(R.id.btnBack);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        apiService = ApiClient.getApiService();

        adapter = new OrderAdapter(this, new OrderAdapter.OrderListener() {
            @Override
            public void onDelete(String id) {
                huyDH(id);
            }
        });

        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadOrdersFromLocal(); // Hiển thị local trước
        loadord();             // Gọi API cập nhật sau
    }

    private void loadord() {
        Call<List<Order>> call = apiService.getOrderListByUser(userId);

        if (call == null) {
            Log.e(TAG, "API call is null");
            loadOrdersFromLocal();
            return;
        }

        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> apiOrders = response.body();
                    Log.d(TAG, "Đã lấy " + apiOrders.size() + " đơn hàng từ server");
                    Log.d(TAG, "Order API JSON: " + new Gson().toJson(apiOrders));

                    if (!apiOrders.isEmpty()) {
                        adapter.setData(apiOrders);
                        Toast.makeText(Donhang.this, "Đã tải " + apiOrders.size() + " đơn hàng từ server", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Donhang.this, "Không có đơn hàng nào trên server", Toast.LENGTH_SHORT).show();
                        loadOrdersFromLocal(); // fallback
                    }
                } else {
                    Log.e(TAG, "API error: " + response.code());
                    loadOrdersFromLocal();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối API: ", t);
                loadOrdersFromLocal();
            }
        });
    }

    private void loadOrdersFromLocal() {
        List<Order> localOrders = loadOrdersFromLocalAsList();
        if (localOrders.isEmpty()) {
            Toast.makeText(this, "Không có đơn hàng trong bộ nhớ", Toast.LENGTH_SHORT).show();
        } else {
            adapter.setData(localOrders);
            Toast.makeText(this, "Đã tải " + localOrders.size() + " đơn hàng từ bộ nhớ", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Order> loadOrdersFromLocalAsList() {
        SharedPreferences prefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
        String ordersJson = prefs.getString("orders_list", "[]");
        List<Order> orders = new Gson().fromJson(ordersJson, new TypeToken<List<Order>>() {}.getType());

        List<Order> filtered = new ArrayList<>();
        if (orders != null && userId != null) {
            for (Order order : orders) {
                if (userId.equals(order.getId_user())) {
                    filtered.add(order);
                }
            }
        }
        return filtered;
    }

    private void huyDH(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn hàng")
                .setMessage("Bạn có chắc muốn hủy đơn hàng này không?")
                .setPositiveButton("Có", (dialog, which) -> performOrderCancellation(id))
                .setNegativeButton("Không", null)
                .show();
    }

    private void performOrderCancellation(String id) {
        apiService.deleteOrder(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Donhang.this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    loadord();
                } else {
                    updateOrderStatusInLocal(id, "Đã hủy");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                updateOrderStatusInLocal(id, "Đã hủy");
            }
        });
    }

    private void updateOrderStatusInLocal(String orderId, String newStatus) {
        SharedPreferences prefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
        String ordersJson = prefs.getString("orders_list", "[]");

        List<Order> orders = new Gson().fromJson(ordersJson, new TypeToken<List<Order>>() {}.getType());

        if (orders != null) {
            boolean found = false;
            for (Order order : orders) {
                if (order.get_id().equals(orderId)) {
                    order.setStatus(newStatus);
                    found = true;
                    break;
                }
            }

            if (found) {
                prefs.edit().putString("orders_list", new Gson().toJson(orders)).apply();
                Toast.makeText(this, "Đã cập nhật trạng thái đơn hàng", Toast.LENGTH_SHORT).show();
                loadOrdersFromLocal();
            } else {
                Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrdersFromLocal();
        loadord();
    }
}
