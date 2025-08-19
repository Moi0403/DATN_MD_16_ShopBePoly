package com.example.shopbepoly;

import android.content.Context;
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
import com.example.shopbepoly.Adapter.VoucherAdapter;
import com.example.shopbepoly.DTO.Voucher;
import com.example.shopbepoly.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherActivity extends AppCompatActivity implements VoucherAdapter.OnVoucherClickListener {
    private RecyclerView recyclerViewVouchers;
    private LinearLayout layoutEmptyState;
    private TextView tabAvailable, tabSaved, tabUsed;
    private ImageView btnBack;

    private List<Voucher> allVouchers = new ArrayList<>();
    private List<Voucher> availableVouchers = new ArrayList<>();
    private List<Voucher> savedVouchers = new ArrayList<>();
    private List<Voucher> usedVouchers = new ArrayList<>();

    private VoucherAdapter voucherAdapter;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private String currentUserId;

    private int currentTab = 0; // 0: Available, 1: Saved, 2: Used

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher);

        initViews();
        setupRecyclerView();
        setupTabs();
        setupClickListeners();

        apiService = ApiClient.getApiService();
        sharedPreferences = getSharedPreferences("VoucherPrefs", MODE_PRIVATE);

        // Lấy user ID từ session
        SharedPreferences userPrefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        this.currentUserId = userPrefs.getString("userId", "");

        // Kiểm tra nếu chưa đăng nhập
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Kiểm tra xem có selectedTab từ intent không
        int selectedTab = getIntent().getIntExtra("selectedTab", 0);
        currentTab = selectedTab;

        loadVouchers();
    }

    private void initViews() {
        recyclerViewVouchers = findViewById(R.id.recyclerViewVouchers);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tabAvailable = findViewById(R.id.tabAvailable);
        tabSaved = findViewById(R.id.tabSaved);
        tabUsed = findViewById(R.id.tabUsed);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        recyclerViewVouchers.setLayoutManager(new LinearLayoutManager(this));
        voucherAdapter = new VoucherAdapter(this, availableVouchers);
        voucherAdapter.setOnVoucherClickListener(this);
        recyclerViewVouchers.setAdapter(voucherAdapter);
    }

    private void setupTabs() {
        tabAvailable.setOnClickListener(v -> switchTab(0));
        tabSaved.setOnClickListener(v -> switchTab(1));
        tabUsed.setOnClickListener(v -> switchTab(2));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void switchTab(int tabIndex) {
        currentTab = tabIndex;

        // Reset tab styles
        resetTabStyles();

        // Set selected tab style
        switch (tabIndex) {
            case 0:
                tabAvailable.setTextColor(getResources().getColor(R.color.orange));
                tabAvailable.setBackgroundResource(R.drawable.tab_selected_bg);
                showVouchers(availableVouchers);
                break;
            case 1:
                tabSaved.setTextColor(getResources().getColor(R.color.orange));
                tabSaved.setBackgroundResource(R.drawable.tab_selected_bg);
                loadSavedVouchers();
                break;
            case 2:
                tabUsed.setTextColor(getResources().getColor(R.color.orange));
                tabUsed.setBackgroundResource(R.drawable.tab_selected_bg);
                loadUsedVouchers();
                break;
        }
    }

    private void resetTabStyles() {
        int grayColor = getResources().getColor(R.color.gray);
        int unselectedBg = R.drawable.tab_unselected_bg;

        tabAvailable.setTextColor(grayColor);
        tabAvailable.setBackgroundResource(unselectedBg);

        tabSaved.setTextColor(grayColor);
        tabSaved.setBackgroundResource(unselectedBg);

        tabUsed.setTextColor(grayColor);
        tabUsed.setBackgroundResource(unselectedBg);
    }

    private void loadVouchers() {
        apiService.getVouchers().enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allVouchers.clear();
                    allVouchers.addAll(response.body());

                    filterAvailableVouchers();
                    switchTab(currentTab);
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                t.printStackTrace();
                showEmptyState();
                Toast.makeText(VoucherActivity.this, "Không thể tải danh sách voucher", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAvailableVouchers() {
        availableVouchers.clear();
        Date currentDate = new Date();

        for (Voucher voucher : allVouchers) {
            if (voucher.isActive() &&
                    voucher.getStartDate().before(currentDate) &&
                    voucher.getEndDate().after(currentDate) &&
                    voucher.getUsedCount() < voucher.getUsageLimit()) {
                availableVouchers.add(voucher);
            }
        }
    }

    private void loadSavedVouchers() {
        savedVouchers.clear();

        if (currentUserId.isEmpty()) {
            showVouchers(savedVouchers);
            return;
        }

        // Key với user ID
        String key = "saved_vouchers_" + currentUserId;
        Set<String> savedVoucherIds = sharedPreferences.getStringSet(key, new HashSet<>());

        for (Voucher voucher : allVouchers) {
            if (savedVoucherIds.contains(voucher.getId())) {
                savedVouchers.add(voucher);
            }
        }

        showVouchers(savedVouchers);
    }

    private void loadUsedVouchers() {
        usedVouchers.clear();

        if (currentUserId.isEmpty()) {
            showVouchers(usedVouchers);
            return;
        }

        // Key với user ID
        String key = "used_vouchers_" + currentUserId;
        Set<String> usedVoucherIds = sharedPreferences.getStringSet(key, new HashSet<>());

        for (Voucher voucher : allVouchers) {
            if (usedVoucherIds.contains(voucher.getId())) {
                usedVouchers.add(voucher);
            }
        }

        showVouchers(usedVouchers);
    }

    private void showVouchers(List<Voucher> vouchers) {
        if (vouchers.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            voucherAdapter = new VoucherAdapter(this, vouchers);
            voucherAdapter.setOnVoucherClickListener(this);
            recyclerViewVouchers.setAdapter(voucherAdapter);
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
    public void onVoucherClick(Voucher voucher) {
        Toast.makeText(this, "Mã: " + voucher.getCode() + " đã được sao chép", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveVoucherClick(Voucher voucher) {
        // Refresh current tab to update voucher counts
        if (currentTab == 0) {
            filterAvailableVouchers();
            showVouchers(availableVouchers);
        } else if (currentTab == 1) {
            loadSavedVouchers();
        }
    }

    @Override
    public void onVoucherUsageLimitReached(Voucher voucher) {
        Toast.makeText(this, "Voucher " + voucher.getCode() + " đã hết lượt sử dụng", Toast.LENGTH_LONG).show();
        loadVouchers();
    }

}