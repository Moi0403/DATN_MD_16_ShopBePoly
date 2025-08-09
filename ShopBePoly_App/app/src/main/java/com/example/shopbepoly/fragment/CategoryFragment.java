package com.example.shopbepoly.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.CategoryAdapter;
import com.example.shopbepoly.Adapter.ProductAdapter;
import com.example.shopbepoly.DTO.Category;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CategoryFragment extends Fragment {

    private ViewPager2 viewPagerCategories;
    private TabLayout tabLayoutCategories;
    private RecyclerView recyclerViewProducts;

    private List<Category> categoryList = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();
    private CategoryAdapter categoryPagerAdapter;
    private ProductAdapter productAdapter;
    private ApiService apiService;

    public CategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        initViews(view);
        setupViewPager();
        setupProductRecyclerView();
        loadCategories();
        loadAllProducts();

        return view;
    }

    private void initViews(View view){
        viewPagerCategories = view.findViewById(R.id.viewPagerCategories);
        tabLayoutCategories = view.findViewById(R.id.tabLayoutCategories);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        apiService = ApiClient.getApiService();
    }

    private void setupViewPager(){
        categoryPagerAdapter = new CategoryAdapter(categoryList, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                loadProductsByCategory(category.get_id());
            }

            @Override
            public void onAllCategoryClick() {
                loadAllProducts();
            }
        });

        viewPagerCategories.setAdapter(categoryPagerAdapter);

        new TabLayoutMediator(tabLayoutCategories, viewPagerCategories,
                (tab, position) -> {
                    if (position == 0){
                        tab.setText("All");
                    } else {
                        tab.setText(categoryList.get(position - 1).getTitle());
                    }
                }).attach();

        tabLayoutCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    loadAllProducts();
                } else {
                    Category category = categoryList.get(position - 1);
                    loadProductsByCategory(category.get_id());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupProductRecyclerView(){
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), productList);
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void loadCategories() {
        Call<List<Category>> call = apiService.getCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body());
                    categoryPagerAdapter.notifyDataSetChanged();

                    // Cập nhật lại TabLayout
                    new TabLayoutMediator(tabLayoutCategories, viewPagerCategories,
                            (tab, position) -> {
                                if (position == 0) {
                                    tab.setText("All");
                                } else {
                                    tab.setText(categoryList.get(position - 1).getTitle());
                                }
                            }).attach();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void loadAllProducts(){
        Call<List<Product>> call = apiService.getProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null){
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