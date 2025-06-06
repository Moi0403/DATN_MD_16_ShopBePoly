package com.example.shopbepoly.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.Adapter.CategoryAdapter;
import com.example.shopbepoly.Adapter.ProductAdapter;
import com.example.shopbepoly.DTO.Category;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public class HomeFragment extends Fragment {
    private RecyclerView recyclerViewProducts, recyclerViewCategories;
    private List<Product> productList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(productList);
        recyclerViewProducts.setAdapter(productAdapter);


        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        LinearLayoutManager layoutManagerCategory = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCategories.setLayoutManager(layoutManagerCategory);
        categoryAdapter = new CategoryAdapter(getContext());
        recyclerViewCategories.setAdapter(categoryAdapter);
        categoryAdapter.setOnCategoryClickListener(category -> {
            loadProductsByCategory(category.get_id());
        });

        loadProduct();
        loadCategories();
        return view;
    }

    private void loadProduct () {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.3:3000/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ProductApi productApi = retrofit.create(ProductApi.class);
        Call<List<Product>> call = productApi.getProducts();
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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.3:3000/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CategoryApi categoryApi = retrofit.create(CategoryApi.class);
        Call<List<Category>> call = categoryApi.getCategories();

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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.3:3000/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ProductApi productApi = retrofit.create(ProductApi.class);
        Call<List<Product>> call = productApi.getProductsByCategory(categoryId);

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

    public interface ProductApi {
        @GET("list_product")
        Call<List<Product>> getProducts();

        @GET("products_by_category/{categoryId}")
        Call<List<Product>> getProductsByCategory(@Path("categoryId") String categoryId);

    }

    public interface CategoryApi {
        @GET("list_category")
        Call<List<Category>> getCategories();
    }


}