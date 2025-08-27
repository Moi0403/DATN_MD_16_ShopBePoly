package com.example.shopbepoly.Screen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewpager2.widget.ViewPager2;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.CartAdapter;
import com.example.shopbepoly.Adapter.ImageSliderAdapter;
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Favorite;
import com.example.shopbepoly.DTO.ListReview;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.RatingResponse;
import com.example.shopbepoly.DTO.Variation;
import com.example.shopbepoly.DanhSachDanhGia;
import com.example.shopbepoly.R;
import com.example.shopbepoly.ThanhToan;
import com.example.shopbepoly.fragment.FavoriteFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChiTietSanPham extends AppCompatActivity {
    private ImageView btnBack, btnFavorite, btnDecrease, btnIncrease, imgProduct, btnCart, star1, star2, star3, star4, star5;
    private TextView tvQuantity, tvProductName, tvPrice, tvDescription, tvKho, tvCateProductName, tvSalePrice, tvOriginalPrice, tvDiscount;
    private AppCompatButton btnAddToCart;
    private TextView size36, size37, size38, size39, size40, size41, size42, size43, size44, size45;
    private int quantity = 1;
    private boolean isFavorite = false;
    private String selectedColor = "";
    private String selectedSize = null;
    private Product product;
    private LinearLayout layoutColorContainer;
    private String selectedColorCode = "";
    private String selectedColorName = "";
    private ViewPager2 viewPagerProductImages;
    private ImageSliderAdapter imageSliderAdapter;
    private CartAdapter cartAdapter;
    private Button btn_AddToPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_san_pham);

        btnCart = findViewById(R.id.btnCart);
        btn_AddToPay = findViewById(R.id.btnAddToCart);
        layoutColorContainer = findViewById(R.id.layoutColorContainer);

        initViews();
        setupClickListeners();

        product = (Product) getIntent().getSerializableExtra("product");

        if (product != null) {
            isFavorite = FavoriteFragment.isFavorite(product);
            updateFavoriteButton();
            loadAverageRating(product.get_id());
        }

        imageSliderAdapter = new ImageSliderAdapter(this, new ArrayList<>());
        viewPagerProductImages.setAdapter(imageSliderAdapter);

        showDefaultProductImages();
        showAvailableColors();
        showAvailableSizes();
        viewPagerProductImages.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPagerProductImages.setUserInputEnabled(true);
        updateUI();
        updateStockDisplay();

        btnCart.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", null);

            if (!validateSelectionForCart()) {
                return; // Validation failed, don't proceed
            }

            Variation selectedVariation = null;
            for (Variation v : product.getVariations()) {
                if (v.getSize() == Integer.parseInt(selectedSize)) {
                    if (selectedColorCode.isEmpty() ||
                            (v.getColor() != null && selectedColorCode.equalsIgnoreCase(v.getColor().getCode()))) {
                        selectedVariation = v;
                        break;
                    }
                }
            }

            Cart cart = new Cart();
            cart.setIdUser(userId);
            cart.setIdProduct(product);
            cart.setQuantity(quantity);
            cart.setPrice(product.getPrice());
            cart.setTotal(product.getPrice() * quantity);
            cart.setSize(Integer.parseInt(selectedSize));
            cart.setStatus(0);

            if (selectedVariation.getColor() != null) {
                cart.setColor(selectedVariation.getColor().getName());
            }

            String image = selectedVariation.getImage();
            if (image != null && !image.trim().isEmpty()) {
                cart.setImg_cart(ApiClient.IMAGE_URL + image.trim());
            } else if (product.getAvt_imgproduct() != null) {
                cart.setImg_cart(ApiClient.IMAGE_URL + product.getAvt_imgproduct());
            }

            Add_Cart(cart);
        });

        btn_AddToPay.setOnClickListener(view1 -> {
            if (validateProductSelection()) {
                navigateToPayment();
            }
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvProductName = findViewById(R.id.tvProductName);
        tvCateProductName = findViewById(R.id.tvCateProductName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvKho = findViewById(R.id.tvKho);
        tvDescription = findViewById(R.id.tvDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        imgProduct = findViewById(R.id.imgProduct);
        viewPagerProductImages = findViewById(R.id.viewPagerProductImages);

        size36 = findViewById(R.id.size36);
        size37 = findViewById(R.id.size37);
        size38 = findViewById(R.id.size38);
        size39 = findViewById(R.id.size39);
        size40 = findViewById(R.id.size40);
        size41 = findViewById(R.id.size41);
        size42 = findViewById(R.id.size42);
        size43 = findViewById(R.id.size43);
        size44 = findViewById(R.id.size44);
        size45 = findViewById(R.id.size45);

        star1 = findViewById(R.id.star1_);
        star2 = findViewById(R.id.star2_);
        star3 = findViewById(R.id.star3_);
        star4 = findViewById(R.id.star4_);
        star5 = findViewById(R.id.star5_);

        star1.setOnClickListener(starClick);
        star2.setOnClickListener(starClick);
        star3.setOnClickListener(starClick);
        star4.setOnClickListener(starClick);
        star5.setOnClickListener(starClick);
    }

    View.OnClickListener starClick = view -> {
        Intent intent = new Intent(ChiTietSanPham.this, DanhSachDanhGia.class);
        intent.putExtra("productId", product.get_id());
        startActivity(intent);
    };

    // Hàm cập nhật hiển thị sao trung bình
    // Cập nhật hiển thị sao + điểm + số lượt đánh giá
    private void updateStarDisplay(float avgRating, int totalReviews) {
        ImageView[] stars = {star1, star2, star3, star4, star5};

        for (int i = 0; i < stars.length; i++) {
            if (avgRating >= i + 1) {
                stars[i].setImageResource(R.drawable.star); // sao đầy
            } else if (avgRating > i && avgRating < i + 1) {
                stars[i].setImageResource(R.drawable.left_half_star); // sao nửa
            } else {
                stars[i].setImageResource(R.drawable.star_filled); // sao rỗng
            }
        }

        TextView txtAverageRating = findViewById(R.id.txtAverageRating);
        txtAverageRating.setText(
                String.format(Locale.getDefault(), "%.1f/5 (%d đánh giá)", avgRating, totalReviews)
        );
    }

    private void loadAverageRating(String productId) {
        ApiService apiService = ApiClient.getApiService();
        Log.d("ChiTietSanPham", "Gọi API: /reviews/average/" + productId);

        apiService.getAverageRating(productId).enqueue(new Callback<RatingResponse>() {
            @Override
            public void onResponse(Call<RatingResponse> call, Response<RatingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    float avgRating = response.body().getAvgRating();
                    int totalReviews = response.body().getTotalReviews();

                    Log.d("ChiTietSanPham", "Kết quả: avg=" + avgRating + ", total=" + totalReviews);
                    updateStarDisplay(avgRating, totalReviews);
                } else {
                    Log.e("ChiTietSanPham", "API lỗi: code=" + response.code());
                }
            }

            @Override
            public void onFailure(Call<RatingResponse> call, Throwable t) {
                Log.e("ChiTietSanPham", "API thất bại: " + t.getMessage(), t);
                Toast.makeText(ChiTietSanPham.this, "Lỗi khi load đánh giá", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFavorite.setOnClickListener(v -> {
            if (product == null) return;
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String userId = prefs.getString("userId", null);
            if (userId == null) {
                Toast.makeText(this, "Bạn cần đăng nhập để yêu thích", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService api = ApiClient.getApiService();
            Favorite fav = new Favorite(userId, product.get_id());
            btnFavorite.setEnabled(false);

            if (isFavorite) {
                api.removeFavorite(userId, product.get_id()).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            isFavorite = false;
                            FavoriteFragment.remove(getApplicationContext(), product);
                            updateFavoriteButton();
                            Toast.makeText(ChiTietSanPham.this, "Xóa sản phẩm yêu thích thành công!", Toast.LENGTH_SHORT).show();
                        }
                        btnFavorite.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        btnFavorite.setEnabled(true);
                    }
                });
            } else {
                api.addFavorite(fav).enqueue(new Callback<Favorite>() {
                    @Override
                    public void onResponse(Call<Favorite> call, Response<Favorite> response) {
                        if (response.isSuccessful()) {
                            isFavorite = true;
                            FavoriteFragment.add(getApplicationContext(), product);
                            updateFavoriteButton();
                            Toast.makeText(ChiTietSanPham.this, "Đã thêm sản phẩm vào yêu thích!", Toast.LENGTH_SHORT).show();
                        }
                        btnFavorite.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Call<Favorite> call, Throwable t) {
                        btnFavorite.setEnabled(true);
                    }
                });
            }
        });

        // IMPROVED: btnDecrease với validation mới
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantity();
            } else {
                Toast.makeText(this, "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
            }
        });

        // IMPROVED: btnIncrease với validation đầy đủ
        btnIncrease.setOnClickListener(v -> {
            // Kiểm tra xem đã chọn màu và size chưa
            if (!isColorAndSizeSelected()) {
                showSelectionRequiredMessage();
                return;
            }

            int maxStock = getMaxAvailableStock();

            if (quantity < maxStock) {
                quantity++;
                updateQuantity();
            } else {
                // Show toast message when trying to exceed stock
                String message = "Không thể thêm quá " + maxStock + " sản phẩm";
                if (!selectedColorCode.isEmpty() && selectedSize != null) {
                    message += " (Màu " + selectedColorName + ", Size " + selectedSize + ")";
                } else if (!selectedColorCode.isEmpty()) {
                    message += " (Màu " + selectedColorName + ")";
                }
                Toast.makeText(ChiTietSanPham.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            if (validateProductSelection()) {
                navigateToPayment();
            }
        });

        size36.setOnClickListener(v -> selectSize("36", size36));
        size37.setOnClickListener(v -> selectSize("37", size37));
        size38.setOnClickListener(v -> selectSize("38", size38));
        size39.setOnClickListener(v -> selectSize("39", size39));
        size40.setOnClickListener(v -> selectSize("40", size40));
        size41.setOnClickListener(v -> selectSize("41", size41));
        size42.setOnClickListener(v -> selectSize("42", size42));
        size43.setOnClickListener(v -> selectSize("43", size43));
        size44.setOnClickListener(v -> selectSize("44", size44));
        size45.setOnClickListener(v -> selectSize("45", size45));
    }

    // NEW METHOD: Kiểm tra xem đã chọn màu và size chưa
    private boolean isColorAndSizeSelected() {
        return !selectedColorCode.isEmpty() && selectedSize != null && !selectedSize.isEmpty();
    }

    // NEW METHOD: Hiển thị thông báo yêu cầu chọn màu và size
    private void showSelectionRequiredMessage() {
        if (selectedColorCode.isEmpty() && (selectedSize == null || selectedSize.isEmpty())) {
            Toast.makeText(this, "Vui lòng chọn màu và size trước khi thay đổi số lượng", Toast.LENGTH_LONG).show();
        } else if (selectedColorCode.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn màu trước khi thay đổi số lượng", Toast.LENGTH_SHORT).show();
        } else if (selectedSize == null || selectedSize.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn size trước khi thay đổi số lượng", Toast.LENGTH_SHORT).show();
        }
    }

    // NEW METHOD: Validation riêng cho việc thêm vào giỏ hàng
    private boolean validateSelectionForCart() {
        if (selectedSize == null || selectedSize.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn size trước khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedColorCode.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn màu trước khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (quantity <= 0) {
            Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        int maxStock = getMaxAvailableStock();
        if (quantity > maxStock) {
            Toast.makeText(this, "Số lượng vượt quá kho có sẵn (" + maxStock + ")", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // EXISTING METHOD: Get maximum available stock based on current selection
    private int getMaxAvailableStock() {
        if (product == null || product.getVariations() == null) {
            return 1; // Default minimum
        }

        int maxStock = 0;

        // Case 1: Both color and size are selected - get specific variation stock
        if (!selectedColorCode.isEmpty() && selectedSize != null) {
            for (Variation variation : product.getVariations()) {
                if (variation.getSize() == Integer.parseInt(selectedSize) &&
                        variation.getColor() != null &&
                        selectedColorCode.equalsIgnoreCase(variation.getColor().getCode())) {
                    maxStock = variation.getStock();
                    break;
                }
            }
        }
        // Case 2: Only color is selected - get total stock for that color
        else if (!selectedColorCode.isEmpty()) {
            for (Variation variation : product.getVariations()) {
                if (variation.getColor() != null &&
                        selectedColorCode.equalsIgnoreCase(variation.getColor().getCode())) {
                    maxStock += variation.getStock();
                }
            }
        }
        // Case 3: No selection - get total stock of all variations
        else {
            for (Variation variation : product.getVariations()) {
                maxStock += variation.getStock();
            }
        }

        return Math.max(maxStock, 1); // Ensure minimum of 1
    }

    // EXISTING METHOD: Validate and adjust quantity when selection changes
    private void validateAndAdjustQuantity() {
        int maxStock = getMaxAvailableStock();
        if (quantity > maxStock) {
            quantity = Math.max(maxStock, 1);
            updateQuantity();
            Toast.makeText(this, "Số lượng đã được điều chỉnh theo kho có sẵn", Toast.LENGTH_SHORT).show();
        }
    }

    // EXISTING METHOD: PHƯƠNG THỨC CHỌN MÀU ĐƯỢC CẢI THIỆN
    private void showAvailableColors() {
        if (product == null || product.getVariations() == null) return;

        layoutColorContainer.removeAllViews();

        Map<String, String> colorMap = new LinkedHashMap<>();
        for (Variation variation : product.getVariations()) {
            if (variation.getColor() != null) {
                String code = variation.getColor().getCode();
                String name = variation.getColor().getName();
                if (!colorMap.containsKey(code)) {
                    colorMap.put(code, name);
                }
            }
        }

        for (Map.Entry<String, String> entry : colorMap.entrySet()) {
            String code = entry.getKey();
            String name = entry.getValue();

            // Container chính cho mỗi màu
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setGravity(Gravity.CENTER);
            itemLayout.setPadding(16, 8, 16, 8);

            // Container cho color circle với shadow effect
            FrameLayout colorContainer = new FrameLayout(this);
            int containerSize = getResources().getDimensionPixelSize(R.dimen.color_circle_size) + 20;
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(containerSize, containerSize);
            containerParams.setMargins(8, 8, 8, 4);
            colorContainer.setLayoutParams(containerParams);

            // Shadow view (tạo hiệu ứng đổ bóng)
            View shadowView = new View(this);
            FrameLayout.LayoutParams shadowParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            shadowParams.setMargins(2, 2, 0, 0);
            shadowView.setLayoutParams(shadowParams);
            shadowView.setBackgroundResource(R.drawable.color_circle_background);
            shadowView.setAlpha(0.3f);
            shadowView.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

            // Main color circle
            View colorCircle = new View(this);
            int circleSize = getResources().getDimensionPixelSize(R.dimen.color_circle_size);
            FrameLayout.LayoutParams circleParams = new FrameLayout.LayoutParams(circleSize, circleSize);
            circleParams.gravity = Gravity.CENTER;
            colorCircle.setLayoutParams(circleParams);
            colorCircle.setBackgroundResource(R.drawable.color_circle_background);
            colorCircle.setTag(code);

            // Set màu cho circle với border màu trắng
            try {
                int color = Color.parseColor(code);
                // Tạo gradient drawable với border trắng
                android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
                drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                drawable.setColor(color);
                drawable.setStroke(3, Color.WHITE); // Border trắng 3px
                colorCircle.setBackground(drawable);
            } catch (IllegalArgumentException e) {
                colorCircle.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            }

            // Checkmark icon (ẩn ban đầu)
            ImageView checkMark = new ImageView(this);
            FrameLayout.LayoutParams checkParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            checkParams.gravity = Gravity.CENTER;
            checkMark.setLayoutParams(checkParams);
            checkMark.setImageResource(R.drawable.ic_check_white);
            checkMark.setVisibility(View.GONE);
            checkMark.setTag("checkmark_" + code);

            // Text tên màu với styling
            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextSize(12);
            tvName.setTextColor(Color.BLACK);
            tvName.setGravity(Gravity.CENTER);
            tvName.setTypeface(null, android.graphics.Typeface.NORMAL);
            tvName.setPadding(4, 4, 4, 4);

            // Sự kiện click cho color circle
            colorCircle.setOnClickListener(v -> {
                String clickedCode = (String) v.getTag();
                handleColorSelection(clickedCode, name);
            });

            // Assemble views
            colorContainer.addView(shadowView);
            colorContainer.addView(colorCircle);
            colorContainer.addView(checkMark);

            itemLayout.addView(colorContainer);
            itemLayout.addView(tvName);
            layoutColorContainer.addView(itemLayout);
        }
    }

    // EXISTING METHOD: Phương thức xử lý chọn màu được cải thiện
    private void handleColorSelection(String clickedCode, String colorName) {
        if (clickedCode.equals(selectedColorCode)) {
            // Bỏ chọn màu hiện tại
            selectedColorCode = "";
            selectedColorName = "";
            selectedSize = null;
            quantity = 1; // Reset quantity về 1 khi bỏ chọn màu
            highlightSelectedColor("");
            showDefaultProductImages();
            resetSizeSelections();
        } else {
            // Chọn màu mới
            selectedColorCode = clickedCode;
            selectedColorName = colorName;
            selectedSize = null;
            quantity = 1; // Reset quantity về 1 khi chọn màu mới
            highlightSelectedColor(clickedCode);
            updateImageByColor(clickedCode);
            resetSizeSelections();
        }

        showAvailableSizes();
        updateStockDisplay();
        updateQuantity(); // Update quantity display
        validateAndAdjustQuantity();
    }

    // EXISTING METHOD: Phương thức highlight được cải thiện
    private void highlightSelectedColor(String selectedCode) {
        for (int i = 0; i < layoutColorContainer.getChildCount(); i++) {
            LinearLayout itemLayout = (LinearLayout) layoutColorContainer.getChildAt(i);
            if (itemLayout.getChildCount() >= 2) {
                FrameLayout colorContainer = (FrameLayout) itemLayout.getChildAt(0);
                TextView nameText = (TextView) itemLayout.getChildAt(1);

                View colorCircle = colorContainer.getChildAt(1); // Main color circle
                ImageView checkMark = (ImageView) colorContainer.getChildAt(2);
                String tagCode = (String) colorCircle.getTag();

                if (selectedCode != null && selectedCode.equals(tagCode)) {
                    // Highlight selected color
                    try {
                        int color = Color.parseColor(tagCode);
                        android.graphics.drawable.GradientDrawable selectedDrawable = new android.graphics.drawable.GradientDrawable();
                        selectedDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                        selectedDrawable.setColor(color);
                        selectedDrawable.setStroke(5, Color.parseColor("#FF4444")); // Red border for selection
                        colorCircle.setBackground(selectedDrawable);

                        // Show checkmark
                        checkMark.setVisibility(View.VISIBLE);

                        // Bold text
                        nameText.setTypeface(null, android.graphics.Typeface.BOLD);
                        nameText.setTextColor(Color.parseColor("#FF4444"));
                    } catch (IllegalArgumentException e) {
                        colorCircle.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    }
                } else {
                    // Reset to unselected state
                    try {
                        int color = Color.parseColor(tagCode);
                        android.graphics.drawable.GradientDrawable normalDrawable = new android.graphics.drawable.GradientDrawable();
                        normalDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                        normalDrawable.setColor(color);
                        normalDrawable.setStroke(3, Color.WHITE); // White border
                        colorCircle.setBackground(normalDrawable);

                        // Hide checkmark
                        checkMark.setVisibility(View.GONE);

                        // Normal text
                        nameText.setTypeface(null, android.graphics.Typeface.NORMAL);
                        nameText.setTextColor(Color.BLACK);
                    } catch (IllegalArgumentException e) {
                        colorCircle.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    }
                }
            }
        }
    }

    // EXISTING METHOD: CẬP NHẬT PHƯƠNG THỨC updateStockDisplay()
    private void updateStockDisplay() {
        if (product == null || product.getVariations() == null) return;

        if (selectedColorCode.isEmpty()) {
            int totalStock = 0;
            for (Variation variation : product.getVariations()) {
                totalStock += variation.getStock();
            }
            tvKho.setText("Kho: " + totalStock);
            return;
        }

        if (selectedSize == null || selectedSize.isEmpty()) {
            int colorStock = 0;
            for (Variation variation : product.getVariations()) {
                if (variation.getColor() != null && selectedColorCode.equalsIgnoreCase(variation.getColor().getCode())) {
                    colorStock += variation.getStock();
                }
            }
            tvKho.setText("Kho: " + colorStock + " (Màu " + selectedColorName + ")");
            return;
        }

        for (Variation variation : product.getVariations()) {
            if (variation.getSize() == Integer.parseInt(selectedSize) &&
                    variation.getColor() != null &&
                    selectedColorCode.equalsIgnoreCase(variation.getColor().getCode())) {
                tvKho.setText("Kho: " + variation.getStock() + " (Màu " + selectedColorName + ", Size " + selectedSize + ")");
                return;
            }
        }

        tvKho.setText("Kho: 0 (Hết hàng)");
    }

    // EXISTING METHOD: Validation for product selection
    private boolean validateProductSelection() {
        if (product == null) {
            Toast.makeText(this, "Không có thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedColorCode == null || selectedColorCode.isEmpty()){
            Toast.makeText(this, "Vui lòng chọn màu", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedSize == null || selectedSize.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (quantity <= 0) {
            Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if quantity exceeds available stock
        int maxStock = getMaxAvailableStock();
        if (quantity > maxStock) {
            Toast.makeText(this, "Số lượng vượt quá kho có sẵn (" + maxStock + ")", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (product != null) {
            fetchProductFromServer(product.get_id());
        }
    }

    private void fetchProductFromServer(String productId) {
        ApiService apiService = ApiClient.getApiService();
        apiService.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    product = response.body();
                    updateUI();
                    showAvailableColors();
                    showAvailableSizes();
                    updateStockDisplay();
                    showDefaultProductImages();
                    validateAndAdjustQuantity();
                } else {
                    Log.e("FetchProduct", "Không lấy được sản phẩm");
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Log.e("FetchProduct", "Lỗi khi gọi API", t);
            }
        });
    }

    private void navigateToPayment() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", null);

            Variation selectedVariation = null;
            for (Variation v : product.getVariations()) {
                if (v.getSize() == Integer.parseInt(selectedSize)) {
                    if (selectedColorCode.isEmpty() ||
                            (v.getColor() != null && selectedColorCode.equalsIgnoreCase(v.getColor().getCode()))) {
                        selectedVariation = v;
                        break;
                    }
                }
            }

            if (selectedVariation == null) {
                Toast.makeText(this, "Không tìm thấy biến thể phù hợp", Toast.LENGTH_SHORT).show();
                return;
            }

            Cart cart = new Cart();
            cart.setIdUser(userId);
            cart.setIdProduct(product);
            cart.setQuantity(quantity);
            cart.setPrice(product.getPrice());
            cart.setTotal(product.getPrice() * quantity);
            cart.setSize(Integer.parseInt(selectedSize));
            cart.setStatus(0);

            if (selectedVariation.getColor() != null) {
                cart.setColor(selectedVariation.getColor().getName());
            }

            String image = selectedVariation.getImage();
            if (image != null && !image.trim().isEmpty()) {
                cart.setImg_cart(ApiClient.IMAGE_URL + image.trim());
            } else if (product.getAvt_imgproduct() != null) {
                cart.setImg_cart(ApiClient.IMAGE_URL + product.getAvt_imgproduct());
            }

            List<Cart> selectedItems = new ArrayList<>();
            selectedItems.add(cart);

            String jsonCart = new Gson().toJson(selectedItems);
            Intent intent = new Intent(ChiTietSanPham.this, ThanhToan.class);
            intent.putExtra("cart_list", jsonCart);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("ChiTietSanPham", "Lỗi chuyển sang thanh toán", e);
            Toast.makeText(this, "Lỗi khi chuyển sang thanh toán", Toast.LENGTH_SHORT).show();
        }
    }

    private void logPaymentData() {
        Log.d("ChiTietSanPham", "=== Payment Data ===");
        Log.d("ChiTietSanPham", "Product ID: " + product.get_id());
        Log.d("ChiTietSanPham", "Product name: " + product.getNameproduct());
        Log.d("ChiTietSanPham", "Price: " + product.getPrice());
        Log.d("ChiTietSanPham", "Quantity: " + quantity);
        Log.d("ChiTietSanPham", "Size: " + selectedSize);
        Log.d("ChiTietSanPham", "Color: " + selectedColor);
        Log.d("ChiTietSanPham", "==================");
    }

    private void selectSize(String size, TextView sizeView) {
        selectedSize = size;
        resetSizeSelections();
        sizeView.setBackgroundResource(R.drawable.size_selector_selected);
        sizeView.setTextColor(getResources().getColor(android.R.color.white));
        updateStockDisplay();
        validateAndAdjustQuantity();
    }

    private void resetSizeSelections() {
        size36.setBackgroundResource(R.drawable.size_selector);
        size37.setBackgroundResource(R.drawable.size_selector);
        size38.setBackgroundResource(R.drawable.size_selector);
        size39.setBackgroundResource(R.drawable.size_selector);
        size40.setBackgroundResource(R.drawable.size_selector);
        size41.setBackgroundResource(R.drawable.size_selector);
        size42.setBackgroundResource(R.drawable.size_selector);
        size43.setBackgroundResource(R.drawable.size_selector);
        size44.setBackgroundResource(R.drawable.size_selector);
        size45.setBackgroundResource(R.drawable.size_selector);

        int defaultTextColor = getResources().getColor(R.color.size_text_default);
        size36.setTextColor(defaultTextColor);
        size37.setTextColor(defaultTextColor);
        size38.setTextColor(defaultTextColor);
        size39.setTextColor(defaultTextColor);
        size40.setTextColor(defaultTextColor);
        size41.setTextColor(defaultTextColor);
        size42.setTextColor(defaultTextColor);
        size43.setTextColor(defaultTextColor);
        size44.setTextColor(defaultTextColor);
        size45.setTextColor(defaultTextColor);
    }

    private void updateQuantity() {
        tvQuantity.setText(String.valueOf(quantity));
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_heart_filled);
            btnFavorite.setColorFilter(getResources().getColor(R.color.heart_color));
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart_outline);
            btnFavorite.setColorFilter(getResources().getColor(R.color.heart_outline_color));
        }
    }

    private void showAvailableSizes() {
        if (product == null || product.getVariations() == null) return;

        size36.setVisibility(View.GONE);
        size37.setVisibility(View.GONE);
        size38.setVisibility(View.GONE);
        size39.setVisibility(View.GONE);
        size40.setVisibility(View.GONE);
        size41.setVisibility(View.GONE);
        size42.setVisibility(View.GONE);
        size43.setVisibility(View.GONE);
        size44.setVisibility(View.GONE);
        size45.setVisibility(View.GONE);

        resetSizeSelections();

        for (Variation variation : product.getVariations()) {
            int size = variation.getSize();
            int stock = variation.getStock();
            String colorCode = variation.getColor() != null ? variation.getColor().getCode() : "";

            if (stock > 0) {
                if (selectedColorCode.isEmpty() || colorCode.equals(selectedColorCode)) {
                    switch (size) {
                        case 36: size36.setVisibility(View.VISIBLE); break;
                        case 37: size37.setVisibility(View.VISIBLE); break;
                        case 38: size38.setVisibility(View.VISIBLE); break;
                        case 39: size39.setVisibility(View.VISIBLE); break;
                        case 40: size40.setVisibility(View.VISIBLE); break;
                        case 41: size41.setVisibility(View.VISIBLE); break;
                        case 42: size42.setVisibility(View.VISIBLE); break;
                        case 43: size43.setVisibility(View.VISIBLE); break;
                        case 44: size44.setVisibility(View.VISIBLE); break;
                        case 45: size45.setVisibility(View.VISIBLE); break;
                    }
                }
            }
        }
    }

    private void updateUI() {
        updateQuantity();
        updateFavoriteButton();

        if (product != null) {
            tvProductName.setText(product.getNameproduct());
            tvDescription.setText(product.getDescription());
            tvCateProductName.setText("(" + product.getCategoryName() + ")");

            int originalPrice = product.getPrice();
            int price_sale = product.getPrice_sale();
            int discount = product.getSale();

            if (discount > 0) {
                SpannableString originalPriceStr = new SpannableString(product.getFormattedPrice());
                originalPriceStr.setSpan(new StrikethroughSpan(), 0, originalPriceStr.length(), 0);
                originalPriceStr.setSpan(
                        new ForegroundColorSpan(getResources().getColor(android.R.color.darker_gray)),
                        0,
                        originalPriceStr.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                SpannableString finalPriceStr = new SpannableString(String.format("%,d đ", price_sale));
                finalPriceStr.setSpan(
                        new ForegroundColorSpan(getResources().getColor(R.color.heart_color)),
                        0,
                        finalPriceStr.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                tvPrice.setText(TextUtils.concat(originalPriceStr, "  ", finalPriceStr));
                tvDiscount.setVisibility(View.VISIBLE);
                tvDiscount.setText(discount + "%");

            } else {
                SpannableString redPrice = new SpannableString(product.getFormattedPrice());
                redPrice.setSpan(
                        new ForegroundColorSpan(getResources().getColor(R.color.heart_color)),
                        0,
                        redPrice.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                tvPrice.setText(redPrice);
                tvDiscount.setVisibility(View.GONE);
            }
        }
    }

    private void Add_Cart(Cart cart){
        ApiService apiService = ApiClient.getApiService();
        Call<Cart> call = apiService.addCart(cart);
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChiTietSanPham.this, "Thêm giỏ hàng thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("AddCartError", "Response code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(ChiTietSanPham.this, "Thêm giỏ hàng không thành công", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Log.e("AddCartError", t.getMessage(), t);
                Toast.makeText(ChiTietSanPham.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateImageByColor(String code) {
        if (product == null || product.getVariations() == null) return;

        Set<String> uniqueImages = new LinkedHashSet<>();
        String avtImageUrl = ApiClient.IMAGE_URL + product.getAvt_imgproduct().trim();

        for (Variation v : product.getVariations()) {
            if (v.getColor() != null && code.equalsIgnoreCase(v.getColor().getCode())) {
                Log.d("ImageSlider", "→ matched color: " + code);

                if (v.getImage() != null && !v.getImage().trim().isEmpty()) {
                    String fullUrl = ApiClient.IMAGE_URL + v.getImage().trim();
                    if (!fullUrl.equals(avtImageUrl)) {
                        uniqueImages.add(fullUrl);
                    }
                }

                if (v.getList_imgproduct() != null && !v.getList_imgproduct().isEmpty()) {
                    for (String img : v.getList_imgproduct()) {
                        if (img != null && !img.trim().isEmpty()) {
                            String fullUrl = ApiClient.IMAGE_URL + img.trim();
                            if (!fullUrl.equals(avtImageUrl)) {
                                uniqueImages.add(fullUrl);
                            }
                        }
                    }
                }
            }
        }

        List<String> finalImages;
        if (!uniqueImages.isEmpty()) {
            Log.d("ImageSlider", "✅ Có ảnh theo màu riêng: " + uniqueImages.size());

            if (uniqueImages.size() == 1) {
                String onlyImage = uniqueImages.iterator().next();
                uniqueImages.add(onlyImage);
            }

            finalImages = new ArrayList<>(uniqueImages);
        } else {
            Log.d("ImageSlider", "⚠️ Không có ảnh riêng theo màu — giữ ảnh mặc định");
            Toast.makeText(this, "Không có ảnh riêng cho màu này", Toast.LENGTH_SHORT).show();
            return;
        }

        imageSliderAdapter = new ImageSliderAdapter(this, finalImages);
        viewPagerProductImages.setAdapter(imageSliderAdapter);
        viewPagerProductImages.setCurrentItem(0, false);
    }

    private void showDefaultProductImages() {
        Set<String> imageSet = new LinkedHashSet<>();

        if (product.getAvt_imgproduct() != null && !product.getAvt_imgproduct().trim().isEmpty()) {
            imageSet.add(ApiClient.IMAGE_URL + product.getAvt_imgproduct().trim());
        }

        if (product.getList_imgproduct() != null) {
            for (String img : product.getList_imgproduct()) {
                if (img != null && !img.trim().isEmpty()) {
                    String fullUrl = ApiClient.IMAGE_URL + img.trim();
                    imageSet.add(fullUrl);
                }
            }
        }

        List<String> finalImages = new ArrayList<>(imageSet);

        if (!finalImages.isEmpty()) {
            Log.d("ImageSlider", "Hiển thị ảnh mặc định (avt lên đầu): " + finalImages.size());
            imageSliderAdapter.updateImages(finalImages);
            viewPagerProductImages.setCurrentItem(0, false);
        }
    }

    private void resetSizeSelection() {
        selectedSize = null;
        resetSizeSelections();
        updateStockDisplay();
    }
}