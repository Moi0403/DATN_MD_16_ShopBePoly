package com.example.shopbepoly.fragment;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.BannerAdapter;
import com.example.shopbepoly.Adapter.ProductAdapter;
import com.example.shopbepoly.DTO.Banner;
import com.example.shopbepoly.DTO.Notification;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.Voucher;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.ThongBao;
import com.example.shopbepoly.Screen.TimKiem;
import com.example.shopbepoly.Screen.LoginScreen;
import com.example.shopbepoly.VoucherActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerViewProducts;
    private List<Product> productList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private ApiService apiService;
    private LinearLayout searchBox;
    private ViewPager2 viewPagerBanner;
    private BannerAdapter bannerAdapter;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;
    private static final long DELAY_MS = 3000;
    private ImageView img_notify;
    private static final String CHANNEL_ID = "shopbepoly_channel";
    private static final int NOTIFICATION_ID = 1;
    private TextView tvNotificationCount;
    private TextView tvViewAllVouchers;
    private LinearLayout btnMoreVouchers;
    private LinearLayout layoutVoucherCards;

    private List<Banner> bannerList = new ArrayList<>();
    private List<Voucher> voucherList = new ArrayList<>();

    // Thống nhất với VoucherActivity - sử dụng userId
    private SharedPreferences voucherPrefs;
    private SharedPreferences loginPrefs;
    private String currentUserId;

    private final ActivityResultLauncher<Intent> thongBaoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            updateNotificationCount();
                        }
                    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        FavoriteFragment.loadFavoritesFromPrefs(requireContext());

        // Initialize SharedPreferences
        voucherPrefs = requireContext().getSharedPreferences("VoucherPrefs", Context.MODE_PRIVATE);
        loginPrefs = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        // Lấy userId hiện tại
        currentUserId = loginPrefs.getString("userId", "");

        // Initialize views
        initializeViews(view);

        // Setup listeners
        setupListeners();

        // Initialize banner and load data
        setupBanner();
        loadProduct();
        loadVouchers();

        createNotificationChannel();

        return view;
    }

    private void initializeViews(View view) {
        img_notify = view.findViewById(R.id.imgNotification);
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);
        tvViewAllVouchers = view.findViewById(R.id.tvViewAllVouchers);
        btnMoreVouchers = view.findViewById(R.id.btnMoreVouchers);
        layoutVoucherCards = view.findViewById(R.id.layoutVoucherCards);
        searchBox = view.findViewById(R.id.searchBox);
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);

        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), productList);
        recyclerViewProducts.setAdapter(productAdapter);
        apiService = ApiClient.getApiService();
    }

    private void setupListeners() {
        // Setup notification click
        img_notify.setOnClickListener(v -> {
            if (currentUserId.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để xem thông báo", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), LoginScreen.class));
                return;
            }

            apiService.getNotifications(currentUserId).enqueue(new Callback<List<Notification>>() {
                @Override
                public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                    Intent intent = new Intent(getActivity(), ThongBao.class);
                    thongBaoLauncher.launch(intent);
                }

                @Override
                public void onFailure(Call<List<Notification>> call, Throwable t) {
                    t.printStackTrace();
                    startActivity(new Intent(getActivity(), ThongBao.class));
                }
            });
        });

        // Setup search click
        searchBox.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TimKiem.class);
            startActivity(intent);
        });

        // Setup voucher section clicks
        tvViewAllVouchers.setOnClickListener(v -> {
            if (currentUserId.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để sử dụng tính năng voucher", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), LoginScreen.class));
                return;
            }

            Intent intent = new Intent(getActivity(), VoucherActivity.class);
            startActivity(intent);
        });

        btnMoreVouchers.setOnClickListener(v -> {
            if (currentUserId.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để sử dụng tính năng voucher", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), LoginScreen.class));
                return;
            }

            Intent intent = new Intent(getActivity(), VoucherActivity.class);
            startActivity(intent);
        });
    }

    private void setupBanner() {
        bannerAdapter = new BannerAdapter(bannerList, ApiClient.BASE_URL);
        viewPagerBanner.setAdapter(bannerAdapter);
        fetchBanners();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (bannerList.isEmpty()) {
                    return;
                }
                currentPage = (currentPage + 1) % bannerList.size();
                viewPagerBanner.setCurrentItem(currentPage, true);
                handler.postDelayed(this, DELAY_MS);
            }
        };

        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
            }
        });
    }

    private void updateNotificationCount() {
        if (currentUserId.isEmpty()) {
            tvNotificationCount.setVisibility(View.GONE);
            return;
        }

        apiService.getNotifications(currentUserId).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int unreadCount = 0;
                    for (Notification notification : response.body()) {
                        if (!notification.isRead()) {
                            unreadCount++;
                        }
                    }

                    if (unreadCount > 0) {
                        tvNotificationCount.setText(String.valueOf(unreadCount));
                        tvNotificationCount.setVisibility(View.VISIBLE);
                    } else {
                        tvNotificationCount.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // Method load vouchers từ API
    private void loadVouchers() {
        Log.d("HomeFragment", "Starting to load vouchers...");

        apiService.getVouchers().enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    voucherList.clear();
                    voucherList.addAll(response.body());
                    Log.d("HomeFragment", "Loaded " + voucherList.size() + " vouchers from API");
                    displayVouchers();
                } else {
                    Log.e("HomeFragment", "Error loading vouchers: " + response.code() + " - " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("HomeFragment", "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                Log.e("HomeFragment", "Network error loading vouchers", t);
            }
        });
    }

    // Method hiển thị vouchers
    private void displayVouchers() {
        Log.d("HomeFragment", "displayVouchers() called");

        if (layoutVoucherCards == null) {
            Log.w("HomeFragment", "layoutVoucherCards is null");
            return;
        }

        if (voucherList == null || voucherList.isEmpty()) {
            Log.w("HomeFragment", "Voucher list is null or empty");
            return;
        }

        try {
            // Lưu lại button "Xem thêm"
            View moreButton = layoutVoucherCards.findViewById(R.id.btnMoreVouchers);
            if (moreButton != null) {
                layoutVoucherCards.removeView(moreButton);
            }

            // Xóa tất cả voucher cards cũ
            layoutVoucherCards.removeAllViews();

            // Hiển thị tối đa 2 voucher active đầu tiên
            int displayCount = 0;
            int maxDisplay = 2;

            for (Voucher voucher : voucherList) {
                if (displayCount >= maxDisplay) break;

                // Kiểm tra voucher còn active và chưa hết hạn
                if (isVoucherValid(voucher)) {
                    try {
                        View voucherCard = createVoucherCard(voucher);
                        if (voucherCard != null) {
                            layoutVoucherCards.addView(voucherCard);
                            displayCount++;
                            Log.d("HomeFragment", "Added voucher card: " + voucher.getCode());
                        }
                    } catch (Exception e) {
                        Log.e("HomeFragment", "Error creating voucher card for: " + voucher.getCode(), e);
                    }
                }
            }

            // Thêm lại button "Xem thêm"
            if (moreButton != null) {
                layoutVoucherCards.addView(moreButton);
            }

            Log.d("HomeFragment", "Successfully displayed " + displayCount + " active vouchers");

        } catch (Exception e) {
            Log.e("HomeFragment", "Error in displayVouchers()", e);
        }
    }

    // Helper method để kiểm tra voucher có hợp lệ không
    private boolean isVoucherValid(Voucher voucher) {
        if (voucher == null) return false;

        // Kiểm tra active
        if (!voucher.isActive()) return false;

        // Kiểm tra hết hạn
        Date currentDate = new Date();
        if (voucher.getEndDate() != null && currentDate.after(voucher.getEndDate())) {
            return false;
        }

        // Kiểm tra usage limit
        if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return false;
        }

        // THÊM: Kiểm tra voucher đã được sử dụng bởi user hiện tại chưa
        // Nếu đã sử dụng thì không hiển thị nữa
        if (isVoucherUsedByCurrentUser(voucher.getId())) {
            return false;
        }

        return true;
    }

    // Method tạo voucher card động
    private View createVoucherCard(Voucher voucher) {
        try {
            if (getContext() == null || voucher == null) {
                Log.w("HomeFragment", "Context or voucher is null");
                return null;
            }

            // Tạo layout chính
            LinearLayout voucherCard = new LinearLayout(getContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    (int) (280 * getResources().getDisplayMetrics().density),
                    (int) (100 * getResources().getDisplayMetrics().density)
            );
            cardParams.setMarginEnd((int) (12 * getResources().getDisplayMetrics().density));
            voucherCard.setLayoutParams(cardParams);
            voucherCard.setOrientation(LinearLayout.HORIZONTAL);
            voucherCard.setBackground(getResources().getDrawable(R.drawable.voucher_card_bg));

            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            voucherCard.setPadding(padding, padding, padding, padding);
            voucherCard.setGravity(16); // center_vertical

            // Icon
            ImageView icon = createVoucherIcon();

            // Text container
            LinearLayout textContainer = createTextContainer(voucher);

            // Save button
            TextView saveButton = createSaveButton(voucher);

            // Add views to containers
            voucherCard.addView(icon);
            voucherCard.addView(textContainer);
            voucherCard.addView(saveButton);

            return voucherCard;

        } catch (Exception e) {
            Log.e("HomeFragment", "Error creating voucher card", e);
            return null;
        }
    }

    private ImageView createVoucherIcon() {
        ImageView icon = new ImageView(getContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                (int) (40 * getResources().getDisplayMetrics().density),
                (int) (40 * getResources().getDisplayMetrics().density)
        );
        iconParams.setMarginEnd((int) (12 * getResources().getDisplayMetrics().density));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.ic_voucher_ticket);
        icon.setBackground(getResources().getDrawable(R.drawable.circle_orange_bg));
        int iconPadding = (int) (8 * getResources().getDisplayMetrics().density);
        icon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        return icon;
    }

    private LinearLayout createTextContainer(Voucher voucher) {
        LinearLayout textContainer = new LinearLayout(getContext());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );
        textContainer.setLayoutParams(textParams);
        textContainer.setOrientation(LinearLayout.VERTICAL);

        // Voucher title
        TextView title = new TextView(getContext());
        title.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Format discount text
        String discountText = formatDiscountText(voucher);
        title.setText(discountText);
        title.setTextColor(Color.parseColor("#FF6B35"));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextSize(16);

        // Voucher description
        TextView description = new TextView(getContext());
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        description.setLayoutParams(descParams);

        String descText = formatDescriptionText(voucher);
        description.setText(descText);
        description.setTextColor(Color.parseColor("#666666"));
        description.setTextSize(12);

        textContainer.addView(title);
        textContainer.addView(description);

        return textContainer;
    }

    private TextView createSaveButton(Voucher voucher) {
        TextView saveButton = new TextView(getContext());
        saveButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Kiểm tra user đã đăng nhập chưa
        if (currentUserId.isEmpty()) {
            saveButton.setText("Đăng nhập");
            saveButton.setTextColor(Color.WHITE);
            saveButton.setBackground(getResources().getDrawable(R.drawable.save_voucher_bg));
            saveButton.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để lưu voucher", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), LoginScreen.class));
            });
        } else {
            // Vì voucher đã used sẽ không hiển thị ở HomeFragment (đã lọc trong isVoucherValid)
            // nên chỉ cần kiểm tra đã lưu hay chưa
            boolean isSaved = isVoucherSaved(voucher.getId());

            if (isSaved) {
                // Voucher đã được lưu nhưng chưa sử dụng
                saveButton.setText("Đã lưu");
                saveButton.setTextColor(getResources().getColor(R.color.gray));
                saveButton.setBackground(getResources().getDrawable(R.drawable.saved_button_bg));
                saveButton.setEnabled(false);
            } else {
                // Voucher có thể lưu
                saveButton.setText("Lưu");
                saveButton.setTextColor(Color.WHITE);
                saveButton.setBackground(getResources().getDrawable(R.drawable.save_voucher_bg));

                // Add click listener cho save button
                saveButton.setOnClickListener(v -> {
                    saveVoucher(voucher);
                    saveButton.setText("Đã lưu");
                    saveButton.setTextColor(getResources().getColor(R.color.gray));
                    saveButton.setBackground(getResources().getDrawable(R.drawable.saved_button_bg));
                    saveButton.setEnabled(false);
                    Toast.makeText(getContext(), "Đã lưu mã giảm giá!", Toast.LENGTH_SHORT).show();
                });
            }
        }

        saveButton.setTypeface(null, android.graphics.Typeface.BOLD);
        int buttonPadding = (int) (8 * getResources().getDisplayMetrics().density);
        saveButton.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
        saveButton.setTextSize(12);

        return saveButton;
    }

    private String formatDiscountText(Voucher voucher) {
        if ("percent".equals(voucher.getDiscountType()) || "percent".equals(voucher.getDiscountType())) {
            return "Giảm " + (int) voucher.getDiscountValue() + "%";
        } else {
            return "Giảm " + formatCurrency(voucher.getDiscountValue());
        }
    }

    private String formatDescriptionText(Voucher voucher) {
        if ("percent".equals(voucher.getDiscountType()) || "percent".equals(voucher.getDiscountType())) {
            if (voucher.getMinOrderValue() > 0) {
                return "Đơn tối thiểu " + formatCurrency(voucher.getMinOrderValue());
            } else {
                return "Không giới hạn";
            }
        } else {
            if (voucher.getMinOrderValue() > 0) {
                return "Đơn tối thiểu " + formatCurrency(voucher.getMinOrderValue());
            } else {
                return "Không giới hạn";
            }
        }
    }

    // Method kiểm tra voucher đã được lưu chưa - THỐNG NHẤT với VoucherActivity
    private boolean isVoucherSaved(String voucherId) {
        if (currentUserId.isEmpty()) {
            return false;
        }

        // Sử dụng key với userId giống như VoucherActivity
        String key = "saved_vouchers_" + currentUserId;
        Set<String> savedVouchers = voucherPrefs.getStringSet(key, new HashSet<>());
        return savedVouchers.contains(voucherId);
    }

    // THÊM METHOD MỚI: Kiểm tra voucher đã được sử dụng chưa - THỐNG NHẤT với ThanhToan
    private boolean isVoucherUsedByCurrentUser(String voucherId) {
        if (currentUserId.isEmpty()) {
            return false;
        }

        // Sử dụng key với userId giống như trong ThanhToan.markVoucherAsUsed()
        String key = "used_vouchers_" + currentUserId;
        Set<String> usedVouchers = voucherPrefs.getStringSet(key, new HashSet<>());
        return usedVouchers.contains(voucherId);
    }

    // Method lưu voucher - THỐNG NHẤT với VoucherActivity
    private void saveVoucher(Voucher voucher) {
        if (currentUserId.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để lưu voucher", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sử dụng key với userId giống như VoucherActivity
        String key = "saved_vouchers_" + currentUserId;
        Set<String> savedVouchers = voucherPrefs.getStringSet(key, new HashSet<>());
        savedVouchers = new HashSet<>(savedVouchers); // Create new set to avoid modification issues
        savedVouchers.add(voucher.getId());
        voucherPrefs.edit().putStringSet(key, savedVouchers).apply();

        Log.d("HomeFragment", "Saved voucher: " + voucher.getCode() + " with ID: " + voucher.getId() + " for user: " + currentUserId);
    }

    // Helper method format currency
    private String formatCurrency(double amount) {
        if (amount >= 1000000) {
            return (int) (amount / 1000000) + "M";
        } else if (amount >= 1000) {
            return (int) (amount / 1000) + "K";
        }
        return String.valueOf((int) amount);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Cập nhật lại userId khi resume
        currentUserId = loginPrefs.getString("userId", "");

        if (handler != null && runnable != null && !bannerList.isEmpty()) {
            handler.postDelayed(runnable, DELAY_MS);
        }

        FavoriteFragment.loadFavoritesFromPrefs(requireContext());

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
        updateNotificationCount();

        // Reload vouchers để cập nhật trạng thái "đã lưu" và "đã sử dụng"
        if (!voucherList.isEmpty()) {
            displayVouchers();
        } else {
            loadVouchers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void loadProduct() {
        Call<List<Product>> call = apiService.getProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    productAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ShopBePoly Notification Channel";
            String description = "Kênh thông báo cho ShopBePoly";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void fetchBanners() {
        apiService.getBanners().enqueue(new Callback<List<Banner>>() {
            @Override
            public void onResponse(@NonNull Call<List<Banner>> call, @NonNull Response<List<Banner>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bannerList.clear();
                    bannerList.addAll(response.body());
                    bannerAdapter.notifyDataSetChanged();

                    if (!bannerList.isEmpty()) {
                        handler.postDelayed(runnable, DELAY_MS);
                    }
                } else {
                    Log.e("HomeFragment", "Error loading banners: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Banner>> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "Network error loading banners", t);
            }
        });
    }
}