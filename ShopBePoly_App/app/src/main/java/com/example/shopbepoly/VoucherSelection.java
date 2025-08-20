package com.example.shopbepoly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.VoucherSelectionAdapter;
import com.example.shopbepoly.DTO.Voucher;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherSelection extends AppCompatActivity implements VoucherSelectionAdapter.OnVoucherSelectionListener {

    private RecyclerView recyclerViewVouchers;
    private LinearLayout layoutEmptyState;
    private ImageView btnBack;
    private TextView tvOrderInfo;

    private List<Voucher> allVouchers = new ArrayList<>();
    private List<Voucher> applicableVouchers = new ArrayList<>();

    private VoucherSelectionAdapter voucherAdapter;
    private ApiService apiService;
    private SharedPreferences voucherPrefs;
    private String currentUserId;

    private double orderTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_selection);

        initViews();
        setupClickListeners();

        apiService = ApiClient.getApiService();
        voucherPrefs = getSharedPreferences("VoucherPrefs", MODE_PRIVATE);

        // Lấy user ID từ session
        SharedPreferences userPrefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        currentUserId = userPrefs.getString("userId", "");

        // Lấy tổng đơn hàng từ intent
        orderTotal = getIntent().getDoubleExtra("order_total", 0);

        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hiển thị thông tin đơn hàng
        updateOrderInfo();

        loadApplicableVouchers();
    }

    private void initViews() {
        recyclerViewVouchers = findViewById(R.id.recyclerViewVouchers);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnBack = findViewById(R.id.btnBack);
        tvOrderInfo = findViewById(R.id.tvOrderInfo);

        recyclerViewVouchers.setLayoutManager(new LinearLayoutManager(this));
        voucherAdapter = new VoucherSelectionAdapter(this, applicableVouchers, orderTotal);
        voucherAdapter.setOnVoucherSelectionListener(this);
        recyclerViewVouchers.setAdapter(voucherAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateOrderInfo() {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        String formattedAmount = formatter.format(orderTotal) + "₫";
        tvOrderInfo.setText("Tổng tiền đơn hàng: " + formattedAmount);
    }

    private void loadApplicableVouchers() {
        apiService.getVouchers().enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allVouchers.clear();
                    allVouchers.addAll(response.body());

                    filterApplicableVouchers();
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                t.printStackTrace();
                showEmptyState();
                Toast.makeText(VoucherSelection.this, "Không thể tải danh sách voucher", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterApplicableVouchers() {
        applicableVouchers.clear();

        // Lấy danh sách voucher đã lưu
        String key = "saved_vouchers_" + currentUserId;
        Set<String> savedVoucherIds = voucherPrefs.getStringSet(key, new HashSet<>());

        Date currentDate = new Date();
        List<Voucher> eligibleVouchers = new ArrayList<>();
        List<Voucher> ineligibleVouchers = new ArrayList<>();

        for (Voucher voucher : allVouchers) {
            // Kiểm tra voucher đã được lưu
            if (savedVoucherIds.contains(voucher.getId())) {
                // Kiểm tra voucher còn hiệu lực
                if (voucher.isActive() &&
                        voucher.getStartDate().before(currentDate) &&
                        voucher.getEndDate().after(currentDate) &&
                        voucher.getUsedCount() < voucher.getUsageLimit()) {

                    // Phân loại theo điều kiện đơn hàng
                    if (orderTotal >= voucher.getMinOrderValue()) {
                        eligibleVouchers.add(voucher);
                    } else {
                        ineligibleVouchers.add(voucher);
                    }
                }
            }
        }

        // Sắp xếp: voucher có thể dùng trước, không thể dùng sau
        applicableVouchers.addAll(eligibleVouchers);
        applicableVouchers.addAll(ineligibleVouchers);

        if (applicableVouchers.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            voucherAdapter.updateData(applicableVouchers);
        }
    }

    private void showEmptyState() {
        recyclerViewVouchers.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        recyclerViewVouchers.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    @Override
    public void onVoucherSelected(Voucher voucher) {
        // Kiểm tra điều kiện một lần nữa
        if (orderTotal >= voucher.getMinOrderValue()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_voucher", new Gson().toJson(voucher));
            setResult(RESULT_OK, resultIntent);

            // Hiển thị thông báo xác nhận
            double discount = calculateDiscount(voucher, orderTotal);
            String message = String.format("Đã chọn voucher! Bạn sẽ tiết kiệm %s",
                    formatCurrency(discount));
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            finish();
        } else {
            double needed = voucher.getMinOrderValue() - orderTotal;
            String message = String.format("Cần mua thêm %s để đạt đơn tối thiểu %s",
                    formatCurrency(needed), formatCurrency(voucher.getMinOrderValue()));
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private double calculateDiscount(Voucher voucher, double orderTotal) {
        if ("percent".equals(voucher.getDiscountType()) || "percentage".equals(voucher.getDiscountType())) {
            return orderTotal * (voucher.getDiscountValue() / 100);
        } else {
            return voucher.getDiscountValue();
        }
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(amount) + "₫";
    }
}