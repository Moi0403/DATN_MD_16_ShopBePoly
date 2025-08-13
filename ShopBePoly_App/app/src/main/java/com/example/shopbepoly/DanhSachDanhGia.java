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
    private String currentUserId;

    private ListReviewAdapter adapter;
    private List<ListReview> allReviews = new ArrayList<>();

    private SharedPreferences prefs;
    private Gson gson = new Gson();

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

        currentUserId = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .getString("userId", null);

        productId = getIntent().getStringExtra("productId");
        orderId = getIntent().getStringExtra("orderId");

        if ((productId == null || productId.isEmpty()) && (orderId == null || orderId.isEmpty())) {
            Toast.makeText(this, "Không tìm thấy thông tin để lấy đánh giá", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListReviewAdapter(this, this::showEditDialog);
        adapter.setCurrentUserId(currentUserId);
        rvReviews.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                adapter.setReviews(allReviews);
                return;
            }
            Chip chip = findViewById(checkedId);
            String text = chip.getText().toString();

            if (text.equalsIgnoreCase("Tất cả")) {
                adapter.setReviews(allReviews);
            } else {
                try {
                    int star = Integer.parseInt(text.replace("★", "").trim());
                    List<ListReview> filtered = new ArrayList<>();
                    for (ListReview r : allReviews) {
                        if (r.getRating() == star) {
                            filtered.add(r);
                        }
                    }
                    adapter.setReviews(filtered);
                } catch (NumberFormatException e) {
                    adapter.setReviews(allReviews);
                }
            }
        });

        // Load dữ liệu từ cache trước
        loadCachedReviews();

        // Sau đó load API để cập nhật mới
        loadReviewsFromApi();
    }

    private void loadCachedReviews() {
        String key = getCacheKey();
        String json = prefs.getString(key, null);
        if (json != null) {
            Type type = new TypeToken<List<ListReview>>() {}.getType();
            List<ListReview> cachedList = gson.fromJson(json, type);
            allReviews.clear();
            allReviews.addAll(cachedList);
            adapter.setReviews(allReviews);
        }
    }

    private void saveReviewsToCache() {
        String key = getCacheKey();
        String json = gson.toJson(allReviews);
        prefs.edit().putString(key, json).apply();
    }

    private String getCacheKey() {
//        if (productId != null && !productId.isEmpty()) {
//            return "reviews_product_" + productId;
//        } else {
//            return "reviews_order_" + orderId;
//        }
        return "reviews_all";
    }

    private void loadReviewsFromApi() {
        ApiService apiService = ApiClient.getApiService();
        Call<List<ListReview>> call = apiService.getAllReviews(); // API mới lấy tất cả review

        call.enqueue(new Callback<List<ListReview>>() {
            @Override
            public void onResponse(Call<List<ListReview>> call, Response<List<ListReview>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allReviews.clear();
                    allReviews.addAll(response.body());

                    // Sắp xếp từ mới nhất -> cũ nhất
                    allReviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));

                    adapter.setReviews(allReviews);
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

    private void mergeReviews(List<ListReview> fetched) {
        for (ListReview review : fetched) {
            if (!allReviews.contains(review)) {
                allReviews.add(review); // Thêm mới nếu chưa có
            } else {
                // Nếu đã có thì cập nhật nội dung mới
                int index = allReviews.indexOf(review);
                allReviews.set(index, review);
            }
        }
    }

    private void showEditDialog(ListReview review) {
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

                    ApiClient.getApiService().updateReview(review.get_id(), newRating, newComment)
                            .enqueue(new Callback<ListReview>() {
                                @Override
                                public void onResponse(Call<ListReview> call, Response<ListReview> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Toast.makeText(DanhSachDanhGia.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                        loadReviewsFromApi();
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