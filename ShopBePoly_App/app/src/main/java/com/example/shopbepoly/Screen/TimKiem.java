package com.example.shopbepoly.Screen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.Adapter.ProductAdapter;
import com.example.shopbepoly.Adapter.SearchHistoryAdapter;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimKiem extends AppCompatActivity {
    private EditText etSearch;
    private ImageButton btnBack;
    private TextView btnClearHistory;
    private RecyclerView rvSearchResults;
    private RecyclerView rvSearchHistory;
    private RecyclerView rvSuggestedProducts;
    private ProductAdapter productAdapter;
    private SearchHistoryAdapter historyAdapter;
    private ApiService apiService;
    private LinearLayout searchHistoryContainer, searchResultsContainer;
    private TextView tvSuggestedProducts;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "SearchHistory";
    private static final String KEY_HISTORY = "history";
    private static final int MAX_HISTORY_ITEMS = 10;
    
    // Add debouncing for search
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY = 500; // 500ms delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tim_kiem);

        // Initialize views
        etSearch = findViewById(R.id.et_search);
        btnBack = findViewById(R.id.btn_back);
        btnClearHistory = findViewById(R.id.btn_clear_history);
        rvSearchResults = findViewById(R.id.rv_search_results);
        rvSearchHistory = findViewById(R.id.rv_search_history);
        rvSuggestedProducts = findViewById(R.id.rv_suggested_products);
        searchResultsContainer = findViewById(R.id.search_results_container);
        searchHistoryContainer = findViewById(R.id.search_history_container);
        tvSuggestedProducts = findViewById(R.id.tv_suggested_products);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Setup RecyclerViews
        rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(TimKiem.this,new ArrayList<>());
        rvSearchResults.setAdapter(productAdapter);
        
        rvSearchHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new SearchHistoryAdapter(query -> {
            etSearch.setText(query);
            showSearchResults(query);
        });
        historyAdapter.setOnDeleteItemClickListener(query -> {
            onDeleteHistoryItem(query);
        });
        rvSearchHistory.setAdapter(historyAdapter);
        
        rvSuggestedProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvSuggestedProducts.setAdapter(productAdapter);
        
        // Initialize API service
        apiService = ApiClient.getApiService();
        
        // Setup search functionality
        setupSearch();
        
        // Setup back button
        btnBack.setOnClickListener(v -> finish());
        
        // Setup clear history button
        btnClearHistory.setOnClickListener(v -> clearSearchHistory());
        
        // Show search history and suggested products initially
        showSearchHistory();

    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                if (query.isEmpty()) {
                    showSearchHistory();
                } else {
                    // Debounce search with delay
                    searchRunnable = () -> showSearchResults(query);
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
                }
            }
        });
    }

    private void showSearchHistory() {
        // Load and show search history
        String historyJson = sharedPreferences.getString(KEY_HISTORY, "[]");
        List<String> historyList = new ArrayList<>();
        
        // Parse the JSON string to get the list
        if (!historyJson.equals("[]")) {
            String[] items = historyJson.substring(1, historyJson.length() - 1).split(",");
            for (String item : items) {
                if (!item.isEmpty()) {
                    historyList.add(item.replace("\"", "").trim());
                }
            }
        }
        
        // Update adapter with history data
        historyAdapter.updateData(historyList);
        
        // Show clear history button if there's history
        btnClearHistory.setVisibility(historyList.isEmpty() ? View.GONE : View.VISIBLE);
        
        // Show search history container
        searchHistoryContainer.setVisibility(View.VISIBLE);
        
        // Hide search results
        searchResultsContainer.setVisibility(View.GONE);
        
        // Show suggested products
        tvSuggestedProducts.setVisibility(View.VISIBLE);
        loadSuggestedProducts();
    }

    private void showSearchResults(String query) {
        // Save to search history
        saveToSearchHistory(query);

        // Show search results container
        searchHistoryContainer.setVisibility(View.GONE);
        searchResultsContainer.setVisibility(View.VISIBLE);

        // Use the search API endpoint instead of local filtering
        apiService.searchProduct(query).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> searchResults = response.body();
                    productAdapter.setData(searchResults);
                    
                    // Show message if no results found
                    if (searchResults.isEmpty()) {
                        Toast.makeText(TimKiem.this, "Không tìm thấy sản phẩm phù hợp", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    productAdapter.setData(new ArrayList<>());
                    Toast.makeText(TimKiem.this, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                productAdapter.setData(new ArrayList<>());
                Toast.makeText(TimKiem.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                Log.e("TimKiem", "Search failed", t);
            }
        });
    }

    private void saveToSearchHistory(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        
        try {
            // Get current history as a list
            String historyJson = sharedPreferences.getString(KEY_HISTORY, "[]");
            List<String> historyList = new ArrayList<>();
            
            // Parse the JSON string to get the list
            if (!historyJson.equals("[]")) {
                String[] items = historyJson.substring(1, historyJson.length() - 1).split(",");
                for (String item : items) {
                    if (!item.isEmpty()) {
                        historyList.add(item.replace("\"", "").trim());
                    }
                }
            }
            
            // Remove the query if it exists
            historyList.remove(query);
            
            // Add the new query at the beginning
            historyList.add(0, query);
            
            // Keep only the most recent items
            if (historyList.size() > MAX_HISTORY_ITEMS) {
                historyList = historyList.subList(0, MAX_HISTORY_ITEMS);
            }
            
            // Convert list to JSON string and save
            String newHistoryJson = historyList.toString();
            sharedPreferences.edit().putString(KEY_HISTORY, newHistoryJson).apply();
            
            // Update the UI
            showSearchHistory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSearchHistoryClick(String query) {
        etSearch.setText(query);
        showSearchResults(query);
    }

    private void onDeleteHistoryItem(String query) {
        try {
            // Get current history as a list
            String historyJson = sharedPreferences.getString(KEY_HISTORY, "[]");
            List<String> historyList = new ArrayList<>();
            
            // Parse the JSON string to get the list
            if (!historyJson.equals("[]")) {
                String[] items = historyJson.substring(1, historyJson.length() - 1).split(",");
                for (String item : items) {
                    if (!item.isEmpty()) {
                        historyList.add(item.replace("\"", "").trim());
                    }
                }
            }
            
            // Remove the query
            historyList.remove(query);
            
            // Convert list to JSON string and save
            String newHistoryJson = historyList.toString();
            sharedPreferences.edit().putString(KEY_HISTORY, newHistoryJson).apply();
            
            // Update the UI
            showSearchHistory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearSearchHistory() {
        try {
            sharedPreferences.edit().putString(KEY_HISTORY, "[]").apply();
            historyAdapter.updateData(new ArrayList<>());
            btnClearHistory.setVisibility(View.GONE);
            searchHistoryContainer.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Đã xóa lịch sử tìm kiếm", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSuggestedProducts() {
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> allProducts = response.body();
                    List<Product> suggestedProducts = getRandomProducts(allProducts, 4);
                    productAdapter.setData(suggestedProducts);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("TimKiem", "Error loading suggested products", t);
            }
        });
    }

    private List<Product> getRandomProducts(List<Product> products, int count) {
        if (products == null || products.isEmpty() || count <= 0) {
            return new ArrayList<>();
        }

        List<Product> shuffled = new ArrayList<>(products);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler to prevent memory leaks
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}