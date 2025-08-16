package com.example.shopbepoly.Screen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.Variation;
import com.example.shopbepoly.R;
import com.example.shopbepoly.ThanhToan;
import com.example.shopbepoly.fragment.FavoriteFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChiTietSanPham extends AppCompatActivity {
    private ImageView btnBack, btnFavorite, btnDecrease, btnIncrease, imgProduct, btnCart;
    private TextView tvQuantity, tvProductName, tvPrice, tvDescription, tvKho,tvCateProductName;
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
        }

        // ⚠️ Sửa tại đây
        imageSliderAdapter = new ImageSliderAdapter(this, new ArrayList<>());
        viewPagerProductImages.setAdapter(imageSliderAdapter);

        showDefaultProductImages(); // ← Gọi sau khi adapter đã được gán

        showAvailableColors();
        showAvailableSizes();
        viewPagerProductImages.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPagerProductImages.setUserInputEnabled(true);
        updateUI();
        updateStockDisplay();


        btnCart.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", null);

            if (selectedSize == null || selectedSize.trim().isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn trước khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            } else {
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
                    cart.setColor(selectedVariation.getColor().getName()); // tên màu
                }

                String image = selectedVariation.getImage();
                if (image != null && !image.trim().isEmpty()) {
                    cart.setImg_cart(ApiClient.IMAGE_URL + image.trim());
                } else if (product.getAvt_imgproduct() != null) {
                    cart.setImg_cart(ApiClient.IMAGE_URL + product.getAvt_imgproduct());
                }

                Add_Cart(cart);
            }
        });

        btn_AddToPay.setOnClickListener(view1 -> {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", null);

            if (selectedColorCode == null || selectedColorCode.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn màu trước khi mua hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedSize == null || selectedSize.trim().isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn size trước khi mua hàng", Toast.LENGTH_SHORT).show();
                return;
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

            // Gửi cart dưới dạng list để tương thích với ThanhToan nhận list
            List<Cart> selectedItems = new ArrayList<>();
            selectedItems.add(cart);

            String jsonCart = new Gson().toJson(selectedItems);
            Intent intent = new Intent(ChiTietSanPham.this, ThanhToan.class);
            intent.putExtra("cart_list", jsonCart);
            startActivity(intent);
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

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantity();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            updateQuantity();
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

    // CẬP NHẬT PHƯƠNG THỨC updateStockDisplay() MỚI
    private void updateStockDisplay() {
        if (product == null || product.getVariations() == null) return;

        // Trường hợp 1: Chưa chọn màu - hiển thị tổng tất cả kho
        if (selectedColorCode.isEmpty()) {
            int totalStock = 0;
            for (Variation variation : product.getVariations()) {
                totalStock += variation.getStock();
            }
            tvKho.setText("Kho: " + totalStock);
            return;
        }

        // Trường hợp 2: Đã chọn màu nhưng chưa chọn size - hiển thị tổng kho của màu đó
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

        // Trường hợp 3: Đã chọn cả màu và size - hiển thị kho của variation cụ thể
        for (Variation variation : product.getVariations()) {
            if (variation.getSize() == Integer.parseInt(selectedSize) &&
                    variation.getColor() != null &&
                    selectedColorCode.equalsIgnoreCase(variation.getColor().getCode())) {
                tvKho.setText("Kho: " + variation.getStock() + " (Màu " + selectedColorName + ", Size " + selectedSize + ")");
                return;
            }
        }

        // Trường hợp không tìm thấy variation phù hợp
        tvKho.setText("Kho: 0 (Hết hàng)");
    }

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
                    product = response.body(); // Cập nhật lại thông tin mới nhất
                    updateUI();
                    showAvailableColors();
                    showAvailableSizes();
                    updateStockDisplay();
                    showDefaultProductImages(); // Gọi lại ảnh mặc định
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
            Intent intent = new Intent(this, ThanhToan.class);
            Gson gson = new Gson();
            intent.putExtra("product", gson.toJson(product));
            intent.putExtra("quantity", quantity);
            intent.putExtra("color", selectedColorName); //update color
            intent.putExtra("size", selectedSize);

            //update image
            if (imageSliderAdapter != null && imageSliderAdapter.getImageList() != null && !imageSliderAdapter.getImageList().isEmpty()){
                String selectedImage = imageSliderAdapter.getImageList().get(0);
                intent.putExtra("selectedImage", selectedImage);
            }
            logPaymentData();
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

    // CẬP NHẬT PHƯƠNG THỨC selectSize() MỚI
    private void selectSize(String size, TextView sizeView) {
        selectedSize = size;

        // Reset all size selections
        resetSizeSelections();

        // Set selected size
        sizeView.setBackgroundResource(R.drawable.size_selector_selected);
        sizeView.setTextColor(getResources().getColor(android.R.color.white));

        // Cập nhật hiển thị kho sau khi chọn size
        updateStockDisplay();
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

        // Ẩn trước
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

        resetSizeSelections(); // reset border + màu

        for (Variation variation : product.getVariations()) {
            int size = variation.getSize();
            int stock = variation.getStock();
            String colorCode = variation.getColor() != null ? variation.getColor().getCode() : "";

            if (stock > 0) {
                // Nếu chưa chọn màu, hoặc màu trùng với selectedColorCode
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
            tvCateProductName.setText("("+ product.getCategoryName()+")");
            tvPrice.setText(String.format("%,d đ", product.getPrice()));
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

    // CẬP NHẬT PHƯƠNG THỨC showAvailableColors() MỚI
    private void showAvailableColors() {
        if (product == null || product.getVariations() == null) return;

        layoutColorContainer.removeAllViews(); // Clear trước

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

            // Tạo container cho mỗi màu
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setGravity(Gravity.CENTER);
            itemLayout.setPadding(20, 16, 20, 16); // Tăng padding để dễ chạm

            // Tạo container cho vòng tròn màu với viền
            FrameLayout colorContainer = new FrameLayout(this);
            int containerSize = getResources().getDimensionPixelSize(R.dimen.color_container_size); // 60dp
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(containerSize, containerSize);
            containerParams.setMargins(8, 8, 8, 8);
            colorContainer.setLayoutParams(containerParams);

            // Tạo viền ngoài (border)
            View borderView = new View(this);
            FrameLayout.LayoutParams borderParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            borderView.setLayoutParams(borderParams);
            borderView.setBackgroundResource(R.drawable.color_border_unselected);
            borderView.setTag("border_" + code);

            // Tạo vòng tròn màu bên trong
            View colorCircle = new View(this);
            int circleSize = getResources().getDimensionPixelSize(R.dimen.color_circle_inner_size); // 48dp
            FrameLayout.LayoutParams circleParams = new FrameLayout.LayoutParams(circleSize, circleSize);
            circleParams.gravity = Gravity.CENTER;
            colorCircle.setLayoutParams(circleParams);
            colorCircle.setBackgroundResource(R.drawable.color_circle_inner);

            // Set màu cho vòng tròn
            try {
                int color = Color.parseColor(code);
                colorCircle.setBackgroundTintList(ColorStateList.valueOf(color));
            } catch (IllegalArgumentException e) {
                // Nếu không parse được màu, dùng màu mặc định
                colorCircle.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                Log.w("ColorParsing", "Cannot parse color: " + code);
            }

            colorCircle.setTag(code);

            // Thêm shadow cho vòng tròn màu
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                colorCircle.setElevation(4f);
            }

            // Tạo text tên màu
            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextSize(12);
            tvName.setTextColor(getResources().getColor(R.color.color_name_text));
            tvName.setGravity(Gravity.CENTER);
            tvName.setTypeface(null, android.graphics.Typeface.NORMAL);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.setMargins(0, 8, 0, 0);
            tvName.setLayoutParams(textParams);

            // Thêm hiệu ứng ripple cho Android 5.0+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                colorContainer.setForeground(getDrawable(R.drawable.ripple_effect));
            }

            // Sự kiện click cho container
            colorContainer.setOnClickListener(v -> {
                String clickedCode = (String) colorCircle.getTag();
                handleColorSelection(clickedCode, name);
            });

            // Thêm các view vào container
            colorContainer.addView(borderView);
            colorContainer.addView(colorCircle);

            itemLayout.addView(colorContainer);
            itemLayout.addView(tvName);
            layoutColorContainer.addView(itemLayout);
        }

        // Highlight màu đã chọn nếu có
        if (!selectedColorCode.isEmpty()) {
            highlightSelectedColor(selectedColorCode);
        }
    }

    // Cải thiện phương thức highlightSelectedColor()
    private void highlightSelectedColor(String selectedCode) {
        for (int i = 0; i < layoutColorContainer.getChildCount(); i++) {
            View itemLayout = layoutColorContainer.getChildAt(i);
            if (itemLayout instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) itemLayout;
                FrameLayout colorContainer = (FrameLayout) layout.getChildAt(0);

                // Tìm borderView và colorCircle
                View borderView = null;
                View colorCircle = null;
                TextView nameText = null;

                for (int j = 0; j < colorContainer.getChildCount(); j++) {
                    View child = colorContainer.getChildAt(j);
                    String tag = (String) child.getTag();
                    if (tag != null && tag.startsWith("border_")) {
                        borderView = child;
                    } else if (tag != null && !tag.startsWith("border_")) {
                        colorCircle = child;
                    }
                }

                // Tìm nameText
                if (layout.getChildCount() > 1) {
                    View secondChild = layout.getChildAt(1);
                    if (secondChild instanceof TextView) {
                        nameText = (TextView) secondChild;
                    }
                }

                if (colorCircle != null) {
                    String tagCode = (String) colorCircle.getTag();

                    if (selectedCode != null && selectedCode.equals(tagCode)) {
                        // Màu được chọn - highlight
                        if (borderView != null) {
                            borderView.setBackgroundResource(R.drawable.color_border_selected);
                        }
                        if (nameText != null) {
                            nameText.setTextColor(getResources().getColor(R.color.color_name_selected));
                            nameText.setTypeface(null, android.graphics.Typeface.BOLD);
                        }

                        // Thêm hiệu ứng scale nhẹ
                        colorContainer.animate()
                                .scaleX(1.1f)
                                .scaleY(1.1f)
                                .setDuration(200)
                                .start();

                    } else {
                        // Màu không được chọn - bỏ highlight
                        if (borderView != null) {
                            borderView.setBackgroundResource(R.drawable.color_border_unselected);
                        }
                        if (nameText != null) {
                            nameText.setTextColor(getResources().getColor(R.color.color_name_text));
                            nameText.setTypeface(null, android.graphics.Typeface.NORMAL);
                        }

                        // Reset scale
                        colorContainer.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(200)
                                .start();
                    }
                }
            }
        }
    }

    // Phương thức xử lý chọn màu
    private void handleColorSelection(String clickedCode, String colorName) {
        if (clickedCode.equals(selectedColorCode)) {
            // Bỏ chọn màu - reset về trạng thái ban đầu
            selectedColorCode = "";
            selectedColorName = "";
            selectedSize = null; // Reset size khi bỏ chọn màu
            highlightSelectedColor("");
            showDefaultProductImages();
            resetSizeSelections();
            Toast.makeText(this, "Đã bỏ chọn màu", Toast.LENGTH_SHORT).show();
        } else {
            // Chọn màu mới
            selectedColorCode = clickedCode;
            selectedColorName = colorName;
            selectedSize = null; // Reset size khi chọn màu mới
            highlightSelectedColor(clickedCode);
            updateImageByColor(clickedCode);
            resetSizeSelections(); // Reset size selection UI
            Toast.makeText(this, "Đã chọn màu: " + colorName, Toast.LENGTH_SHORT).show();
        }

        // Cập nhật size và kho sau khi thay đổi màu
        showAvailableSizes();
        updateStockDisplay(); // Quan trọng: cập nhật hiển thị kho
    }
    private void updateImageByColor(String code) {
        if (product == null || product.getVariations() == null) return;

        Set<String> uniqueImages = new LinkedHashSet<>();
        String avtImageUrl = ApiClient.IMAGE_URL + product.getAvt_imgproduct().trim();

        for (Variation v : product.getVariations()) {
            if (v.getColor() != null && code.equalsIgnoreCase(v.getColor().getCode())) {
                Log.d("ImageSlider", "→ matched color: " + code);

                // Lọc ảnh chính nếu khác ảnh đại diện
                if (v.getImage() != null && !v.getImage().trim().isEmpty()) {
                    String fullUrl = ApiClient.IMAGE_URL + v.getImage().trim();
                    if (!fullUrl.equals(avtImageUrl)) {
                        uniqueImages.add(fullUrl);
                    }
                }

                // Lọc danh sách ảnh nếu khác avt
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
                uniqueImages.add(onlyImage);  // nhân đôi ảnh nếu chỉ có 1
            }

            finalImages = new ArrayList<>(uniqueImages);
        } else {
            Log.d("ImageSlider", "⚠️ Không có ảnh riêng theo màu — giữ ảnh mặc định");
            Toast.makeText(this, "Không có ảnh riêng cho màu này", Toast.LENGTH_SHORT).show();
            return; // Không cập nhật adapter nếu không có ảnh riêng
        }

        imageSliderAdapter = new ImageSliderAdapter(this, finalImages);
        viewPagerProductImages.setAdapter(imageSliderAdapter);
        viewPagerProductImages.setCurrentItem(0, false);
    }

    private void showDefaultProductImages() {
        Set<String> imageSet = new LinkedHashSet<>();

        // Thêm ảnh đại diện trước
        if (product.getAvt_imgproduct() != null && !product.getAvt_imgproduct().trim().isEmpty()) {
            imageSet.add(ApiClient.IMAGE_URL + product.getAvt_imgproduct().trim());
        }

        // Sau đó thêm các ảnh giới thiệu (khác avt)
        if (product.getList_imgproduct() != null) {
            for (String img : product.getList_imgproduct()) {
                if (img != null && !img.trim().isEmpty()) {
                    String fullUrl = ApiClient.IMAGE_URL + img.trim();
                    imageSet.add(fullUrl); // Set sẽ không thêm lại nếu trùng avt
                }
            }
        }

        List<String> finalImages = new ArrayList<>(imageSet);

        if (!finalImages.isEmpty()) {
            Log.d("ImageSlider", "Hiển thị ảnh mặc định (avt lên đầu): " + finalImages.size());
            imageSliderAdapter.updateImages(finalImages); // chỉ update
            viewPagerProductImages.setCurrentItem(0, false);
        }
    }
}