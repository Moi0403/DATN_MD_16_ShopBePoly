package com.example.shopbepoly.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.OrderAdapter;
import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LayHangThanhCongFragment extends Fragment {
    private RecyclerView rc_choXN;
    private OrderAdapter orderAdapter;
    private List<Order> list_order = new ArrayList<>();
    private String userId;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dang_lay_hang, container, false);
        rc_choXN = view.findViewById(R.id.rc_choXN);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);


        Context context = getContext();
        if (context == null) {
            Log.e("DangLayHangFragment", "Context is null");
            return view;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để xem đơn hàng", Toast.LENGTH_SHORT).show();
            return view;
        }

        rc_choXN.setLayoutManager(new LinearLayoutManager(context));
        orderAdapter = new OrderAdapter(context, list_order,this::onOrderUpdated);
        rc_choXN.setAdapter(orderAdapter);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (userId != null) {
                LoadOrderUser(userId);
            }
        });
        LoadOrderUser(userId);

        return view;
    }
    private void LoadOrderUser(String userId) {
        ApiService apiService = ApiClient.getApiService();
        Call<List<Order>> call = apiService.getOrderList(userId);
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    list_order.clear();
                    for (Order order : response.body()) {
                        if ("lấy hàng thành công".equalsIgnoreCase(order.getStatus())) {
                            list_order.add(order);
                        }
                    }
                    Log.d("ORDER_DEBUG", "Tổng đơn hàng ĐANG XỬ LÝ: " + list_order.size());
                    orderAdapter.notifyDataSetChanged();
                } else {
                    Log.e("ORDER_ERROR", "Không lấy được dữ liệu đơn hàng");
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("API_FAILURE", "Lỗi API: " + t.getMessage());
            }
        });
    }
    private void onOrderUpdated() {
        if (userId != null) {
            LoadOrderUser(userId);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (userId != null) {
            LoadOrderUser(userId);
        }
    }

}
