package com.example.shopbepoly;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.ListReviewAdapter;
import com.example.shopbepoly.DTO.ListReview;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DanhSachDanhGia extends AppCompatActivity {

    private RecyclerView rvReviews;
    private ImageView btnBack;
    private ChipGroup chipGroupFilter;

    private String productId;
    private String orderId;
    private boolean showAllReviews = false;
    private String currentUserId;

    private ListReviewAdapter adapter;
    private final List<ListReview> allReviews = new ArrayList<>();

    private SharedPreferences prefs;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_danh_sach_danh_gia);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvReviews = findViewById(R.id.rv_reviews);
        btnBack = findViewById(R.id.btn_back);
        chipGroupFilter = findViewById(R.id.chip_group_filter);

        prefs = getSharedPreferences("ReviewCache", MODE_PRIVATE);
        currentUserId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("userId", null);

        productId = getIntent().getStringExtra("productId");
        orderId = getIntent().getStringExtra("orderId");
        showAllReviews = getIntent().getBooleanExtra("showAllReviews", false);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListReviewAdapter(this, this::showEditDialog);
        adapter.setCurrentUserId(currentUserId);
        rvReviews.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        // Bộ lọc theo sao
        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                adapter.setReviews(new ArrayList<>(allReviews));
                return;
            }
            Chip chip = findViewById(checkedId);
            if (chip == null) return;

            String text = chip.getText().toString();
            if (text.equalsIgnoreCase("Tất cả")) {
                adapter.setReviews(new ArrayList<>(allReviews));
            } else {
                try {
                    int star = Integer.parseInt(text.replace("★", "").trim());
                    List<ListReview> filtered = new ArrayList<>();
                    for (ListReview r : allReviews) {
                        if (r != null && r.getRating() == star) {
                            filtered.add(r);
                        }
                    }
                    adapter.setReviews(filtered);
                } catch (NumberFormatException e) {
                    adapter.setReviews(new ArrayList<>(allReviews));
                }
            }
        });

        // Load cache nhanh nếu không phải vừa thêm review
        boolean refreshAfterAdd = getIntent().getBooleanExtra("refreshAfterAdd", false);
        if (!refreshAfterAdd) {
            loadCachedReviews();
        }

        // Luôn gọi API để đồng bộ mới nhất
        loadReviewsFromApi();
    }

    /** Load cache */
    private void loadCachedReviews() {
        String json = prefs.getString(getCacheKey(), null);
        if (json != null) {
            Type type = new TypeToken<List<ListReview>>() {}.getType();
            List<ListReview> cachedList = gson.fromJson(json, type);
            if (cachedList != null && !cachedList.isEmpty()) {
                mergeReviews(cachedList);
                sortReviews();
                adapter.setReviews(new ArrayList<>(allReviews));
            }
        }
    }

    /** Lưu cache */
    private void saveReviewsToCache() {
        prefs.edit().putString(getCacheKey(), gson.toJson(allReviews)).apply();
    }

    private String getCacheKey() {
        if (showAllReviews) return "reviews_all";
        if (productId != null && !productId.isEmpty()) return "reviews_product_" + productId;
        if (orderId != null && !orderId.isEmpty()) return "reviews_order_" + orderId;
        return "reviews_all";
    }

    /** Fetch API */
    private void loadReviewsFromApi() {
        ApiService apiService = ApiClient.getApiService();
        Call<List<ListReview>> call;

        if (showAllReviews) {
            call = apiService.getAllReviews();
        } else if (productId != null && !productId.isEmpty()) {
            call = apiService.getReviews(productId);
        } else if (orderId != null && !orderId.isEmpty()) {
            call = apiService.getReviewsByOrder(orderId);
        } else {
            call = apiService.getAllReviews();
        }

        call.enqueue(new Callback<List<ListReview>>() {
            @Override
            public void onResponse(Call<List<ListReview>> call, Response<List<ListReview>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allReviews.clear(); // 🟢 reset trước khi merge API
                    mergeReviews(response.body());
                    sortReviews();
                    adapter.setReviews(new ArrayList<>(allReviews));
                    saveReviewsToCache();
                } else {
                    Toast.makeText(DanhSachDanhGia.this, "Không lấy được đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ListReview>> call, Throwable t) {
                Toast.makeText(DanhSachDanhGia.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Merge theo _id */
    private void mergeReviews(List<ListReview> newReviews) {
        if (newReviews == null || newReviews.isEmpty()) return;
        for (ListReview newR : newReviews) {
            if (newR == null || newR.getId() == null) continue;
            int idx = findReviewIndexById(newR.getId());
            if (idx == -1) {
                allReviews.add(newR);
            } else {
                allReviews.set(idx, newR);
            }
        }
    }

    private int findReviewIndexById(String id) {
        if (id == null) return -1;
        for (int i = 0; i < allReviews.size(); i++) {
            ListReview r = allReviews.get(i);
            if (r != null && r.getId() != null && r.getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /** Sort theo thời gian */
    private void sortReviews() {
        Collections.sort(allReviews, (r1, r2) -> {
            if (r1 == null || r1.getCreatedAt() == null) return 1;
            if (r2 == null || r2.getCreatedAt() == null) return -1;
            return r2.getCreatedAt().compareTo(r1.getCreatedAt());
        });
    }

    /** Cho phép sửa review */
    private void showEditDialog(ListReview review) {
        if (review == null) return;

        if (currentUserId == null || !currentUserId.equals(review.getUser())) {
            Toast.makeText(this, "Bạn chỉ có thể sửa đánh giá của chính mình", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_review, null);
        EditText edtComment = dialogView.findViewById(R.id.edt_comment);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar_edit);

        edtComment.setText(review.getComment());
        ratingBar.setRating(review.getRating());

        new AlertDialog.Builder(this)
                .setTitle("Sửa đánh giá")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newComment = edtComment.getText().toString().trim();
                    int newRating = (int) ratingBar.getRating();

                    if (newComment.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ApiClient.getApiService().updateReview(review.getId(), newRating, newComment)
                            .enqueue(new Callback<ListReview>() {
                                @Override
                                public void onResponse(Call<ListReview> call, Response<ListReview> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Toast.makeText(DanhSachDanhGia.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                        mergeReviews(Collections.singletonList(response.body()));
                                        sortReviews();
                                        adapter.setReviews(new ArrayList<>(allReviews));
                                        saveReviewsToCache();
                                    } else {
                                        Toast.makeText(DanhSachDanhGia.this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ListReview> call, Throwable t) {
                                    Toast.makeText(DanhSachDanhGia.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}