package com.example.shopbepoly.Screen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.DanhGiaProductAdapter;
import com.example.shopbepoly.DTO.ProductInOrder;
import com.example.shopbepoly.DTO.Review;
import com.example.shopbepoly.DanhSachDanhGia;
import com.example.shopbepoly.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DanhGia extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnSubmit;
    private RecyclerView rvProductList;

    private ArrayList<ProductInOrder> productList;
    private DanhGiaProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_gia);

        // Ánh xạ view
        btnBack = findViewById(R.id.btn_back);
        btnSubmit = findViewById(R.id.btn_submit);
        rvProductList = findViewById(R.id.rv_product_list);

        // Lấy danh sách sản phẩm từ Intent
        productList = (ArrayList<ProductInOrder>) getIntent().getSerializableExtra("listProductInOrder");
        if (productList == null) productList = new ArrayList<>();

        // Setup RecyclerView
        adapter = new DanhGiaProductAdapter(this, productList);
        rvProductList.setLayoutManager(new LinearLayoutManager(this));
        rvProductList.setAdapter(adapter);

        // Nút back
        btnBack.setOnClickListener(v -> finish());

        // Nút gửi đánh giá
        btnSubmit.setOnClickListener(v -> sendAllReviews());
    }

    private void sendAllReviews() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", "");
        String orderId = getIntent().getStringExtra("orderId");

        if (userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy danh sách đánh giá từ adapter
        List<Review> reviewList = adapter.getAllReviews(userId);

        // Kiểm tra dữ liệu
        for (Review r : reviewList) {
            r.setOrderId(orderId);

            if (r.getComment() == null || r.getComment().trim().isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập bình luận cho tất cả sản phẩm!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (r.getRating() <= 0) {
                Toast.makeText(this, "Vui lòng chọn số sao cho tất cả sản phẩm!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (r.getImages() == null) {
                r.setImages(new ArrayList<>());
            }
        }

        // Gọi API gửi batch review
        ApiService apiService = ApiClient.getApiService();
        apiService.sendOrderReviews(orderId, reviewList).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);

                        String message = jsonObject.optString("message", "Hoàn tất gửi đánh giá");
                        Toast.makeText(DanhGia.this, message, Toast.LENGTH_SHORT).show();

                        // Đọc kết quả từng sản phẩm
                        JSONArray results = jsonObject.optJSONArray("results");
                        if (results != null) {
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject item = results.getJSONObject(i);
                                String pid = item.optString("productId");
                                boolean success = item.optBoolean("success");
                                String msg = item.optString("message");

                                if (!success) {
                                    Toast.makeText(DanhGia.this, "SP " + pid + ": " + msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        // ...
                        // Chuyển sang màn danh sách tất cả đánh giá
                        if (!reviewList.isEmpty()) {
                            navigateToAllReviews();
                        } else {
                            finish();
                        }

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DanhGia.this, "Lỗi xử lý dữ liệu trả về", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Lỗi khi gửi đánh giá";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (IOException ignored) {}
                    Toast.makeText(DanhGia.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(DanhGia.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToReviewList(String productId) {
        Intent intent = new Intent(DanhGia.this, DanhSachDanhGia.class);
        intent.putExtra("productId", productId);
        startActivity(intent);
        finish();
    }

    private void navigateToReviewListByOrder(String orderId) {
        Intent intent = new Intent(DanhGia.this, DanhSachDanhGia.class);
        intent.putExtra("orderId", orderId);
        startActivity(intent);
        finish();
    }

    private void navigateToAllReviews() {
        Intent intent = new Intent(DanhGia.this, DanhSachDanhGia.class);
        intent.putExtra("showAllReviews", true);
        intent.putExtra("refreshAfterAdd", true); // <-- thêm flag này
        startActivity(intent);
        finish();
    }
}
