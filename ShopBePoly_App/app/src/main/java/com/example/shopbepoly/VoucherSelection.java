package com.example.shopbepoly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "VoucherSelection";

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

        // Lấy tổng đơn hàng từ intent TRƯỚC TIÊN
        orderTotal = getIntent().getDoubleExtra("order_total", 0);
        Log.d(TAG, "Order total received: " + orderTotal);

        // Kiểm tra nếu orderTotal = 0 thì có thể có vấn đề
        if (orderTotal <= 0) {
            Log.w(TAG, "Warning: Order total is 0 or negative");
            Toast.makeText(this, "Lỗi: Không có thông tin tổng đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();

        apiService = ApiClient.getApiService();
        voucherPrefs = getSharedPreferences("VoucherPrefs", MODE_PRIVATE);

        // Lấy user ID từ session
        SharedPreferences userPrefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        currentUserId = userPrefs.getString("userId", "");

        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hiển thị thông tin đơn hàng
        updateOrderInfo();

        // Load vouchers
        loadApplicableVouchers();
    }

    private void initViews() {
        recyclerViewVouchers = findViewById(R.id.recyclerViewVouchers);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnBack = findViewById(R.id.btnBack);
        tvOrderInfo = findViewById(R.id.tvOrderInfo);

        // Setup RecyclerView
        recyclerViewVouchers.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo adapter với orderTotal đã có
        voucherAdapter = new VoucherSelectionAdapter(this, applicableVouchers, orderTotal);
        voucherAdapter.setOnVoucherSelectionListener(this);
        recyclerViewVouchers.setAdapter(voucherAdapter);

        Log.d(TAG, "Views initialized with orderTotal: " + orderTotal);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateOrderInfo() {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        String formattedAmount = formatter.format(orderTotal) + "₫";
        tvOrderInfo.setText("Tổng tiền đơn hàng: " + formattedAmount);
        Log.d(TAG, "Order info updated: " + formattedAmount);
    }

    private void loadApplicableVouchers() {
        Log.d(TAG, "Loading vouchers...");

        apiService.getVouchers().enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allVouchers.clear();
                    allVouchers.addAll(response.body());
                    Log.d(TAG, "Loaded " + allVouchers.size() + " vouchers from API");

                    filterApplicableVouchers();
                } else {
                    Log.e(TAG, "Failed to load vouchers: " + response.code());
                    showEmptyState();
                    Toast.makeText(VoucherSelection.this, "Không thể tải danh sách voucher", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
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

        Log.d(TAG, "Filtering vouchers for user: " + currentUserId + ", orderTotal: " + orderTotal);
        Log.d(TAG, "Saved voucher IDs: " + savedVoucherIds.size());

        for (Voucher voucher : allVouchers) {
            // Kiểm tra voucher đã được lưu
            if (savedVoucherIds.contains(voucher.getId())) {
                // Kiểm tra voucher còn hiệu lực
                if (voucher.isActive() &&
                        voucher.getStartDate().before(currentDate) &&
                        voucher.getEndDate().after(currentDate) &&
                        voucher.getUsedCount() < voucher.getUsageLimit()) {

                    Log.d(TAG, "Processing voucher: " + voucher.getCode() +
                            " - MinOrder: " + voucher.getMinOrderValue() +
                            " - OrderTotal: " + orderTotal +
                            " - DiscountType: " + voucher.getDiscountType() +
                            " - DiscountValue: " + voucher.getDiscountValue());

                    // Phân loại theo điều kiện đơn hàng
                    if (orderTotal >= voucher.getMinOrderValue()) {
                        eligibleVouchers.add(voucher);
                        Log.d(TAG, "Voucher " + voucher.getCode() + " is eligible");
                    } else {
                        ineligibleVouchers.add(voucher);
                        Log.d(TAG, "Voucher " + voucher.getCode() + " is not eligible (need more: " +
                                (voucher.getMinOrderValue() - orderTotal) + ")");
                    }
                } else {
                    Log.d(TAG, "Voucher " + voucher.getCode() + " is expired or used up");
                }
            } else {
                Log.d(TAG, "Voucher " + voucher.getCode() + " is not saved by user");
            }
        }

        // Sắp xếp: voucher có thể dùng trước, không thể dùng sau
        applicableVouchers.addAll(eligibleVouchers);
        applicableVouchers.addAll(ineligibleVouchers);

        Log.d(TAG, "Final applicable vouchers: " + applicableVouchers.size() +
                " (Eligible: " + eligibleVouchers.size() + ", Ineligible: " + ineligibleVouchers.size() + ")");

        if (applicableVouchers.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            // Cập nhật adapter với danh sách mới và đảm bảo orderTotal đúng
            voucherAdapter.updateData(applicableVouchers);
            voucherAdapter.updateOrderTotal(orderTotal);
        }
    }

    private void showEmptyState() {
        recyclerViewVouchers.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        Log.d(TAG, "Showing empty state");
    }

    private void hideEmptyState() {
        recyclerViewVouchers.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        Log.d(TAG, "Hiding empty state");
    }

    @Override
    public void onVoucherSelected(Voucher voucher) {
        Log.d(TAG, "Voucher selected: " + voucher.getCode() + " for order total: " + orderTotal);

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

            Log.d(TAG, "Voucher selected successfully. Discount: " + discount);
            finish();
        } else {
            double needed = voucher.getMinOrderValue() - orderTotal;
            String message = String.format("Cần mua thêm %s để đạt đơn tối thiểu %s",
                    formatCurrency(needed), formatCurrency(voucher.getMinOrderValue()));
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            Log.w(TAG, "Voucher not applicable. Need more: " + needed);
        }
    }

    /**
     * Tính toán số tiền giảm giá thực tế cho đơn hàng
     * FIXED: Sửa logic tính toán giảm giá để xử lý đúng cả fixed amount và percentage
     */
    private double calculateDiscount(Voucher voucher, double orderTotal) {
        double discount = 0;
        String discountType = voucher.getDiscountType();

        Log.d(TAG, "Calculating discount - Type: " + discountType + ", Value: " + voucher.getDiscountValue() + ", OrderTotal: " + orderTotal);

        // FIXED: Sửa logic xử lý discount type
        if ("percent".equals(discountType) || "percentage".equals(discountType)) {
            // Giảm giá theo phần trăm
            discount = orderTotal * (voucher.getDiscountValue() / 100.0);
            Log.d(TAG, "Percentage discount calculation: " + orderTotal + " * " +
                    (voucher.getDiscountValue() / 100.0) + " = " + discount);
        } else if ("fixed".equals(discountType) || "amount".equals(discountType) || discountType == null || discountType.isEmpty()) {
            // Giảm giá cố định (bao gồm cả trường hợp null hoặc empty)
            discount = voucher.getDiscountValue();
            Log.d(TAG, "Fixed amount discount: " + discount);
        } else {
            // Trường hợp không xác định, mặc định coi là giảm cố định
            discount = voucher.getDiscountValue();
            Log.w(TAG, "Unknown discount type '" + discountType + "', treating as fixed amount: " + discount);
        }

        // Đảm bảo discount không vượt quá tổng đơn hàng
        discount = Math.min(discount, orderTotal);

        Log.d(TAG, "Final calculated discount: " + discount + " for voucher: " + voucher.getCode());
        return discount;
    }

    private String formatCurrency(double amount) {
        try {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            return formatter.format(amount) + "₫";
        } catch (Exception e) {
            Log.w(TAG, "Error formatting currency: " + e.getMessage());
            return String.valueOf((long)amount) + "₫";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity destroyed");
    }
}