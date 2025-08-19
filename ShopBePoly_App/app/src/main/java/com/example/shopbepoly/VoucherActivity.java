package com.example.shopbepoly;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    private int currentTab = 0; // 0: Available, 1: Saved, 2: Used
    private static final String SAVED_VOUCHERS_KEY = "saved_vouchers";
    private static final String USED_VOUCHERS_KEY = "used_vouchers";

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
                    showVouchers(availableVouchers);
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                t.printStackTrace();
                showEmptyState();
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
        Set<String> savedVoucherIds = sharedPreferences.getStringSet(SAVED_VOUCHERS_KEY, new HashSet<>());

        for (Voucher voucher : allVouchers) {
            if (savedVoucherIds.contains(voucher.getId())) {
                savedVouchers.add(voucher);
            }
        }

        showVouchers(savedVouchers);
    }

    private void loadUsedVouchers() {
        usedVouchers.clear();
        Set<String> usedVoucherIds = sharedPreferences.getStringSet(USED_VOUCHERS_KEY, new HashSet<>());

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
        // Handle voucher item click - show voucher details or copy code
        // You can implement a dialog or new activity here
    }

    @Override
    public void onSaveVoucherClick(Voucher voucher) {
        // This is handled in the adapter
    }
}