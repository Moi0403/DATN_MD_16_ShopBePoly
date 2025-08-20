package com.example.shopbepoly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.OrderAdapter;
import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.DTO.DeliveringOrdersResponse;
import com.example.shopbepoly.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppStaff extends AppCompatActivity {
    private RecyclerView recyclerOrders;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OrderAdapter orderAdapter;
    private List<Order> deliveringOrders = new ArrayList<>();
    private ApiService apiService;
    private ImageView imgStaffProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_staff);

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        loadDeliveringOrders();
    }

    private void initViews() {
        recyclerOrders = findViewById(R.id.recyclerOrders);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        imgStaffProfile = findViewById(R.id.imgStaffProfile);
        apiService = ApiClient.getApiService();

        if (imgStaffProfile != null) {
            imgStaffProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AppStaff.this, StaffProfileActivity.class));
                }
            });
        }
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, deliveringOrders, this::onOrderCancelled, true); // true = isStaff
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(orderAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadDeliveringOrders();
        });
    }



    private void loadDeliveringOrders() {
        Call<DeliveringOrdersResponse> call = apiService.getDeliveringOrders();
        call.enqueue(new Callback<DeliveringOrdersResponse>() {
            @Override
            public void onResponse(Call<DeliveringOrdersResponse> call, Response<DeliveringOrdersResponse> response) {
                Log.d("STAFF_DEBUG", "Response code: " + response.code());
                Log.d("STAFF_DEBUG", "Response body: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    DeliveringOrdersResponse deliveringResponse = response.body();

                    if (deliveringResponse.isSuccess()) {
                        deliveringOrders.clear();

                        if (deliveringResponse.getOrders() != null) {
                            deliveringOrders.addAll(deliveringResponse.getOrders());
                            Log.d("AppStaff", "Added " + deliveringResponse.getOrders().size() + " orders to list");
                        }

                        Log.d("STAFF_DEBUG", "Số đơn hàng đang giao: " + deliveringResponse.getCount());
                        Log.d("STAFF_DEBUG", "Danh sách đơn hàng: " + deliveringOrders.size());
                        Log.d("STAFF_DEBUG", "Orders: " + deliveringOrders.toString());

                        orderAdapter.notifyDataSetChanged();
                        Log.d("AppStaff", "Notified adapter of data change, current list size: " + deliveringOrders.size());
                        showEmptyState(deliveringOrders.isEmpty());

                        // Cập nhật header với số lượng đơn hàng
                        updateHeaderWithOrderCount(deliveringOrders.size());

                        // Only show Toast for initial load, not for refresh operations
                        if (!swipeRefreshLayout.isRefreshing()) {
                            if (deliveringOrders.isEmpty()) {
                                Toast.makeText(AppStaff.this, "Không có đơn hàng nào đang giao", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AppStaff.this, "Đã tải " + deliveringOrders.size() + " đơn hàng đang giao", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.e("STAFF_DEBUG", "API trả về success = false: " + deliveringResponse.getError());
                        Toast.makeText(AppStaff.this, "Lỗi: " + deliveringResponse.getError(), Toast.LENGTH_SHORT).show();
                        showEmptyState(true);
                        updateHeaderWithOrderCount(0);
                    }
                } else {
                    Log.e("STAFF_DEBUG", "Response không thành công: " + response.code());
                    Log.e("STAFF_DEBUG", "Error body: " + response.errorBody());
                    Toast.makeText(AppStaff.this, "Lỗi khi tải đơn hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                    updateHeaderWithOrderCount(0);
                }

                // Dừng animation refresh
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<DeliveringOrdersResponse> call, Throwable t) {
                Log.e("STAFF_DEBUG", "Network error: " + t.getMessage());
                Toast.makeText(AppStaff.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState(true);
                updateHeaderWithOrderCount(0);
                // Dừng animation refresh
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showEmptyState(boolean show) {
        if (show) {
            recyclerOrders.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Không có đơn hàng nào đang giao");
        } else {
            recyclerOrders.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void onOrderCancelled() {
        // Reload đơn hàng đang giao khi có đơn hàng bị hủy hoặc xác nhận giao hàng
        Log.d("AppStaff", "onOrderCancelled callback triggered, refreshing order list...");
        loadDeliveringOrders();
        // Removed Toast to prevent callstack issues - the loadDeliveringOrders will show appropriate messages
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh danh sách khi quay lại màn hình
        loadDeliveringOrders();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Cập nhật header khi bắt đầu
        if (deliveringOrders != null) {
            updateHeaderWithOrderCount(deliveringOrders.size());
        }
    }

    private void updateHeaderWithOrderCount(int count) {
        TextView headerTitle = findViewById(R.id.tvHeaderTitle);
        if (headerTitle != null) {
            if (count > 0) {
                headerTitle.setText("Quản lý đơn hàng (" + count + " đơn đang giao)");
            } else {
                headerTitle.setText("Quản lý đơn hàng");
            }
        }
    }
}