package com.example.shopbepoly;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.google.android.material.button.MaterialButton;
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
                loadOrdersFromLocal();
                return;
            }

            call.enqueue(new Callback<List<Order>>() {
                @Override
                public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                    Log.d(TAG, "Response received. Code: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        List<Order> apiOrders = response.body();
                        Log.d(TAG, "Number of orders received: " + apiOrders.size());

                        //load local orders
                        List<Order> localOrders = loadOrdersFromLocalAsList();

                        //merge API and local orders
                        List<Order> allOrders = new ArrayList<>();
                        allOrders.addAll(localOrders);
                        allOrders.addAll(apiOrders);

                        ordList.clear();
                        ordList.addAll(allOrders);
                        adapter.notifyDataSetChanged();

                        int totalOrders = allOrders.size();
                        Toast.makeText(Donhang.this, "Đã tải " + totalOrders + " đơn hàng", Toast.LENGTH_SHORT).show();
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

                        //load from local when Api fails
                        loadOrdersFromLocal();
                    }
                }

                @Override
                public void onFailure(Call<List<Order>> call, Throwable t) {
                    Log.e(TAG, "Error loading orders", t);
                    //load from local when Api fails
                    loadOrdersFromLocal();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadord", e);
            loadOrdersFromLocal();
        }
    }

    private void loadOrdersFromLocal(){
        try {
            SharedPreferences prefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
            String ordersJson = prefs.getString("orders_list", "[]");

            List<Order> localOrders = new Gson().fromJson(ordersJson, new com.google.gson.reflect.TypeToken<List<Order>>() {}.getType());

            if (localOrders != null){
                ordList.clear();
                ordList.addAll(localOrders);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Đã tải " + localOrders.size() + " đơn hàng", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "chưa có đơn hàng nào", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading local orders", e);
            Toast.makeText(this, "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<Order> loadOrdersFromLocalAsList(){
        try {
            SharedPreferences prefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
            String ordersJson = prefs.getString("orders_list", "[]");

            List<Order> localOrders = new Gson().fromJson(ordersJson, new com.google.gson.reflect.TypeToken<List<Order>>() {}.getType());

            return localOrders != null ? localOrders : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error loading local orders as list", e);
            return new ArrayList<>();
        }
    }

    private void huyDH(String id) {
        Log.d(TAG, "Cancelling order: " + id);

        //Hiển thị dialog xác nhận
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    performOrderCancellation(id);
                })
                .setNegativeButton("Không", null)
                .show();
//        try {
//            apiService.deleteOrder(id).enqueue(new Callback<Void>() {
//                @Override
//                public void onResponse(Call<Void> call, Response<Void> response) {
//                    if (response.isSuccessful()) {
//                        Log.d(TAG, "Order cancelled successfully");
//                        loadord();
//                        Toast.makeText(Donhang.this, "Đã hủy", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Log.e(TAG, "Error cancelling order: " + response.code());
//                        Toast.makeText(Donhang.this, "Lỗi hủy: " + response.code(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<Void> call, Throwable t) {
//                    Log.e(TAG, "Error cancelling order", t);
//                    Toast.makeText(Donhang.this, "Lỗi hủy: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        } catch (Exception e) {
//            Log.e(TAG, "Error in huyDH", e);
//            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
    }
    
    private void performOrderCancellation(String id){
        try {
            //thử xóa qua API trước
            apiService.deleteOrder(id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()){
                        Log.d(TAG, "Order cancelled successfully via API");
                        Toast.makeText(Donhang.this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                        loadord(); //tải lại làm mới list
                    } else {
                        Log.e(TAG, "Error cancelling order via API: " + response.code());
                        //thử xóa/cập nhật local
                        updateOrderStatusInLocal(id, "Đã hủy");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Error cancelling order via API", t);
                    // Thử xóa/cập nhật local
                    updateOrderStatusInLocal(id, "Đã hủy");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in performOrderCancellation", e);
            updateOrderStatusInLocal(id, "Đã hủy");
        }
    }

    private void updateOrderStatusInLocal(String orderId, String newStatus){
        try {
            SharedPreferences prefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
            String ordersJson = prefs.getString("orders_list", "[]");

            List<Order> orderList = new Gson().fromJson(ordersJson, new com.google.gson.reflect.TypeToken<List<Order>>() {}.getType());
            
            if (orderList != null){
                boolean found = false;
                for (Order order : orderList) {
                    if (order.get_id().equals(orderId)){
                        order.setStatus(newStatus);
                        found = true;
                        break;
                    }
                }
                
                if (found) {
                    //lưu lại danh sách đã cập nhật
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("orders_list", new Gson().toJson(orderList));
                    editor.apply();

                    Toast.makeText(this, "Đã cập nhật trạng thái đơn hàng", Toast.LENGTH_SHORT).show();
                    loadOrdersFromLocal();
                } else {
                    Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating order status in local", e);
            Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
