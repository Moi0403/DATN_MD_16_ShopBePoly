package com.example.shopbepoly.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import com.example.shopbepoly.DTO.Category;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        FavoriteFragment.loadFavoritesFromPrefs(requireContext());
        img_notify = view.findViewById(R.id.imgNotification);
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
        img_notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ThongBao.class));
            }
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
        // Add banner images to list
        List<Integer> bannerList = new ArrayList<>();
        bannerList.add(R.drawable.banner1);
        bannerList.add(R.drawable.banner2);
        bannerList.add(R.drawable.banner);

        // Setup adapter
        bannerAdapter = new BannerAdapter(bannerList);
        viewPagerBanner.setAdapter(bannerAdapter);

        // Auto scroll banner
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == bannerList.size()) {
                    currentPage = 0;
                }
                viewPagerBanner.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, DELAY_MS);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable, DELAY_MS);

        // Load lại danh sách yêu thích từ SharedPreferences
        FavoriteFragment.loadFavoritesFromPrefs(requireContext());

        // Cập nhật lại adapter để icon yêu thích đổi màu
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
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
}