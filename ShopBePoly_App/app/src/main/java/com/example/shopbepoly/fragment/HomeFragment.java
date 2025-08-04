package com.example.shopbepoly.fragment;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.BannerAdapter;
import com.example.shopbepoly.Adapter.CategoryAdapter;
import com.example.shopbepoly.Adapter.ProductAdapter;
import com.example.shopbepoly.DTO.Banner;
import com.example.shopbepoly.DTO.Category;
import com.example.shopbepoly.DTO.Notification;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.ThongBao;
import com.example.shopbepoly.Screen.TimKiem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerViewProducts, recyclerViewCategories;
    private List<Product> productList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private ApiService apiService;
    private LinearLayout searchBox;
    private ViewPager2 viewPagerBanner;
    private TabLayout tabLayoutBanner;
    private BannerAdapter bannerAdapter;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;
    private static final long DELAY_MS = 3000; // Delay in milliseconds between slides
    private ImageView img_notify;
    private static final String CHANNEL_ID = "shopbepoly_channel";
    private static final int NOTIFICATION_ID = 1;
    private TextView tvNotificationCount;

    private List<Banner> bannerList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> thongBaoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            updateNotificationCount(); // Load lại số thông báo
                        }
                    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        FavoriteFragment.loadFavoritesFromPrefs(requireContext());
        img_notify = view.findViewById(R.id.imgNotification);
        tvNotificationCount = view.findViewById(R.id.tvNotificationCount);

        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(),productList);
        recyclerViewProducts.setAdapter(productAdapter);
        apiService = ApiClient.getApiService();

        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        LinearLayoutManager layoutManagerCategory = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCategories.setLayoutManager(layoutManagerCategory);
        categoryAdapter = new CategoryAdapter(getContext());
        recyclerViewCategories.setAdapter(categoryAdapter);
        categoryAdapter.setOnCategoryClickListener(category -> {
            loadProductsByCategory(category.get_id());
        });
        createNotificationChannel();
        img_notify.setOnClickListener(v -> {
            ApiService apiService = ApiClient.getApiService();
            SharedPreferences prefs = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String userId = prefs.getString("userId", null);

            apiService.getNotifications(userId).enqueue(new Callback<List<Notification>>() {
                @Override
                public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
//                        sendLocalNotification();
                    }
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

        searchBox = view.findViewById(R.id.searchBox);
        searchBox.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TimKiem.class);
            startActivity(intent);
        });

        // Initialize banner views
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);

        // Setup banner
        setupBanner();

        loadProduct();
        loadCategories();
        return view;
    }

    private void setupBanner() {
        bannerAdapter = new BannerAdapter(bannerList, ApiClient.BASE_URL);
        viewPagerBanner.setAdapter(bannerAdapter);
        fetchBanners(); // Gọi API để lấy banner

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
        SharedPreferences prefs = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId == null) return;

        apiService.getNotifications(userId).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body().size();
                    if (count > 0) {
                        tvNotificationCount.setText(String.valueOf(count));
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
    @Override
    public void onResume() {
        super.onResume();
        if (handler != null && runnable != null && !bannerList.isEmpty()) {
            handler.postDelayed(runnable, DELAY_MS);
        }

        // Load lại danh sách yêu thích từ SharedPreferences
        FavoriteFragment.loadFavoritesFromPrefs(requireContext());

        // Cập nhật lại adapter để icon yêu thích đổi màu
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
        updateNotificationCount();
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

    private void loadCategories() {
        Call<List<Category>> call = apiService.getCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body());
                    categoryAdapter.setCategoryList(categoryList);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void loadProductsByCategory(String categoryId) {
        Call<List<Product>> call = apiService.getProductsByCategory(categoryId);
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
                    Log.e("HomeFragment", "Lỗi khi lấy banner: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Banner>> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "Lỗi kết nối khi lấy banner", t);
            }
        });
    }
}