package com.example.shopbepoly.Screen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.ProductAdapter;
import com.example.shopbepoly.Adapter.SearchHistoryAdapter;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimKiem extends AppCompatActivity {
    private EditText etSearch;
    private ImageButton btnBack, btnClearHistory;
    private RecyclerView rvSearchResults, rvSearchHistory;
    private TextView tvResultsCount;
    private ProductAdapter productAdapter;
    private SearchHistoryAdapter historyAdapter;
    private ApiService apiService;
    private LinearLayout searchHistoryContainer, searchResultsContainer;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "SearchHistory";
    private static final String KEY_HISTORY = "history";
    private static final int MAX_HISTORY_ITEMS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tim_kiem);
        
        // Initialize views
        etSearch = findViewById(R.id.et_search);
        btnBack = findViewById(R.id.btn_back);
        btnClearHistory = findViewById(R.id.btn_clear_history);
        rvSearchResults = findViewById(R.id.rv_search_results);
        rvSearchHistory = findViewById(R.id.rv_search_history);
        tvResultsCount = findViewById(R.id.tv_results_count);
        searchHistoryContainer = findViewById(R.id.search_history_container);
        searchResultsContainer = findViewById(R.id.search_results_container);
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        // Setup RecyclerViews
        rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(new ArrayList<>());
        rvSearchResults.setAdapter(productAdapter);
        
        rvSearchHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new SearchHistoryAdapter(new ArrayList<>(), this::onSearchHistoryClick);
        historyAdapter.setOnDeleteItemClickListener(this::onDeleteHistoryItem);
        rvSearchHistory.setAdapter(historyAdapter);
        
        // Initialize API service
        apiService = ApiClient.getApiService();
        
        // Setup search functionality
        setupSearch();
        
        // Setup back button
        btnBack.setOnClickListener(v -> finish());
        
        // Setup clear history button
        btnClearHistory.setOnClickListener(v -> clearSearchHistory());
        
        // Load search history
        loadSearchHistory();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() > 0) {
                    searchProducts(query);
                    searchHistoryContainer.setVisibility(View.GONE);
                    searchResultsContainer.setVisibility(View.VISIBLE);
                } else {
                    productAdapter = new ProductAdapter(new ArrayList<>());
                    rvSearchResults.setAdapter(productAdapter);
                    tvResultsCount.setText("Kết quả tìm kiếm");
                    searchHistoryContainer.setVisibility(View.VISIBLE);
                    searchResultsContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void searchProducts(String keyword) {
        apiService.searchProduct(keyword).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> searchResults = response.body();
                    if (searchResults.isEmpty()) {
                        Toast.makeText(TimKiem.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        tvResultsCount.setText("Không tìm thấy sản phẩm");
                    } else {
                        tvResultsCount.setText("Tìm thấy " + searchResults.size() + " sản phẩm");
                        // Save to search history
                        saveToSearchHistory(keyword);
                    }
                    productAdapter = new ProductAdapter(searchResults);
                    rvSearchResults.setAdapter(productAdapter);
                } else {
                    Toast.makeText(TimKiem.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                    productAdapter = new ProductAdapter(new ArrayList<>());
                    rvSearchResults.setAdapter(productAdapter);
                    tvResultsCount.setText("Không tìm thấy sản phẩm");
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(TimKiem.this, "Lỗi khi tìm kiếm sản phẩm", Toast.LENGTH_SHORT).show();
                productAdapter = new ProductAdapter(new ArrayList<>());
                rvSearchResults.setAdapter(productAdapter);
                tvResultsCount.setText("Lỗi khi tìm kiếm");
            }
        });
    }

    private void saveToSearchHistory(String query) {
        Set<String> history = new HashSet<>(sharedPreferences.getStringSet(KEY_HISTORY, new HashSet<>()));
        history.add(query);
        
        // Keep only the most recent items
        if (history.size() > MAX_HISTORY_ITEMS) {
            List<String> historyList = new ArrayList<>(history);
            history.clear();
            for (int i = historyList.size() - MAX_HISTORY_ITEMS; i < historyList.size(); i++) {
                history.add(historyList.get(i));
            }
        }
        
        sharedPreferences.edit().putStringSet(KEY_HISTORY, history).apply();
        loadSearchHistory();
    }

    private void loadSearchHistory() {
        Set<String> history = sharedPreferences.getStringSet(KEY_HISTORY, new HashSet<>());
        List<String> historyList = new ArrayList<>(history);
        historyAdapter.updateData(historyList);
    }

    private void clearSearchHistory() {
        sharedPreferences.edit().remove(KEY_HISTORY).apply();
        historyAdapter.updateData(new ArrayList<>());
    }

    private void onSearchHistoryClick(String query) {
        etSearch.setText(query);
        searchProducts(query);
    }

    private void onDeleteHistoryItem(String query) {
        Set<String> history = new HashSet<>(sharedPreferences.getStringSet(KEY_HISTORY, new HashSet<>()));
        history.remove(query);
        sharedPreferences.edit().putStringSet(KEY_HISTORY, history).apply();
        loadSearchHistory();
    }
}