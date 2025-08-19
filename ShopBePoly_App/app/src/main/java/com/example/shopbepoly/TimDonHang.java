package com.example.shopbepoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.DTO.ProductInOrder;
import com.example.shopbepoly.Adapter.OrderAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.ArrayList;

public class TimDonHang extends AppCompatActivity {

    private static final String LOGIN_PREFS = "LoginPrefs";
    private static final String USER_ID_KEY = "userId";

    private TextView txtResult;
    private RecyclerView rcvOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private String currentUserId;

    private String getUserIdFromPreferences() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREFS, MODE_PRIVATE);
            return sharedPreferences.getString(USER_ID_KEY, null);
        } catch (Exception e) {
            Log.e("TimDonHang", "Error getting userId from preferences", e);
            return null;
        }
    }

    private void showSearchResults(String query) {
        // Kiểm tra userId
        if (currentUserId == null || currentUserId.isEmpty()) {
            txtResult.setVisibility(View.VISIBLE);
            rcvOrders.setVisibility(View.GONE);
            txtResult.setText("Vui lòng đăng nhập để tìm kiếm đơn hàng");
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        apiService.searchOrdersByCode(query).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> searchResults = response.body();

                    // Lọc chỉ lấy đơn hàng của user hiện tại
                    List<Order> userOrders = new ArrayList<>();
                    for (Order order : searchResults) {
                        if (order.getId_user() != null &&
                                order.getId_user().getId() != null &&
                                order.getId_user().getId().equals(currentUserId)) {
                            userOrders.add(order);
                        }
                    }

                    // Tìm kiếm thêm theo tên sản phẩm
                    List<Order> finalResults = new ArrayList<>();
                    for (Order order : userOrders) {
                        // Kiểm tra mã đơn hàng
                        String id_order = order.getIdOrder() != null ? order.getIdOrder() : order.get_id();
                        boolean matchOrderId = id_order != null &&
                                id_order.toLowerCase().contains(query.toLowerCase());

                        // Kiểm tra tên sản phẩm
                        boolean matchProductName = false;
                        if (order.getProducts() != null) {
                            for (ProductInOrder productInOrder : order.getProducts()) {
                                if (productInOrder.getId_product() != null &&
                                        productInOrder.getId_product().getNameproduct() != null &&
                                        productInOrder.getId_product().getNameproduct().toLowerCase().contains(query.toLowerCase())) {
                                    matchProductName = true;
                                    break;
                                }
                            }
                        }

                        // Thêm vào kết quả nếu khớp mã đơn hàng hoặc tên sản phẩm
                        if (matchOrderId || matchProductName) {
                            finalResults.add(order);
                        }
                    }

                    if (!finalResults.isEmpty()) {
                        // Hiển thị RecyclerView và ẩn TextView
                        txtResult.setVisibility(View.GONE);
                        rcvOrders.setVisibility(View.VISIBLE);

                        // Cập nhật dữ liệu cho adapter
                        orderList.clear();
                        orderList.addAll(finalResults);
                        orderAdapter.notifyDataSetChanged();
                    } else {
                        // Hiển thị thông báo không tìm thấy
                        rcvOrders.setVisibility(View.GONE);
                        txtResult.setVisibility(View.VISIBLE);
                        txtResult.setText("Không tìm thấy đơn hàng phù hợp");
                        Toast.makeText(TimDonHang.this, "Không tìm thấy đơn hàng phù hợp", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Hiển thị lỗi
                    rcvOrders.setVisibility(View.GONE);
                    txtResult.setVisibility(View.VISIBLE);
                    txtResult.setText("Lỗi tìm kiếm");
                    Log.e("TimDonHang", "Search failed: " + response.code() + " - " + response.message());
                    Toast.makeText(TimDonHang.this, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                // Hiển thị lỗi kết nối
                rcvOrders.setVisibility(View.GONE);
                txtResult.setVisibility(View.VISIBLE);
                txtResult.setText("Lỗi kết nối");
                Log.e("TimDonHang", "Search failed", t);
                Toast.makeText(TimDonHang.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tim_don_hang);

        // Lấy userId từ SharedPreferences
        currentUserId = getUserIdFromPreferences();

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        EditText edtSearch = findViewById(R.id.edt_search);
        txtResult = findViewById(R.id.txt_result);
        rcvOrders = findViewById(R.id.rcv_orders);

        // Khởi tạo RecyclerView
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList, () -> {
            // Callback khi đơn hàng bị hủy
            // Có thể refresh lại danh sách nếu cần
        });
        rcvOrders.setLayoutManager(new LinearLayoutManager(this));
        rcvOrders.setAdapter(orderAdapter);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // Ẩn cả TextView và RecyclerView khi không có query
                    txtResult.setVisibility(View.GONE);
                    rcvOrders.setVisibility(View.GONE);
                    orderList.clear();
                    orderAdapter.notifyDataSetChanged();
                    return;
                }
                showSearchResults(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}