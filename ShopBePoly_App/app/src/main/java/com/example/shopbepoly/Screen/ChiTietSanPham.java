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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewpager2.widget.ViewPager2;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
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
    private TextView tvQuantity, tvProductName, tvPrice, tvDescription;
    private AppCompatButton btnAddToCart;
    private TextView size37, size38, size39, size40, size41;
    private int quantity = 1;
    private boolean isFavorite = true;
    private String selectedColor = "white";
    private String selectedSize = "40";
    private Product product;
    private LinearLayout layoutColorContainer;
    private String selectedColorCode = "";
    private ViewPager2 viewPagerProductImages;
    private ImageSliderAdapter imageSliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_san_pham);

        btnCart = findViewById(R.id.btnCart);
        layoutColorContainer = findViewById(R.id.layoutColorContainer);

        initViews();
        setupClickListeners();

        product = (Product) getIntent().getSerializableExtra("product");
        showDefaultProductImages();
        showAvailableColors();
        showAvailableSizes();
        viewPagerProductImages.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPagerProductImages.setUserInputEnabled(true);
        updateUI();

        btnCart.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", null);

            if (userId == null) {
                Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
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
            Add_Cart(cart);
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvProductName = findViewById(R.id.tvProductName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        imgProduct = findViewById(R.id.imgProduct);
        viewPagerProductImages = findViewById(R.id.viewPagerProductImages);

        size37 = findViewById(R.id.size37);
        size38 = findViewById(R.id.size38);
        size39 = findViewById(R.id.size39);
        size40 = findViewById(R.id.size40);
        size41 = findViewById(R.id.size41);
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

        size37.setOnClickListener(v -> selectSize("37", size37));
        size38.setOnClickListener(v -> selectSize("38", size38));
        size39.setOnClickListener(v -> selectSize("39", size39));
        size40.setOnClickListener(v -> selectSize("40", size40));
        size41.setOnClickListener(v -> selectSize("41", size41));
    }

    private boolean validateProductSelection() {
        if (product == null) {
            Toast.makeText(this, "Không có thông tin sản phẩm", Toast.LENGTH_SHORT).show();
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

    private void navigateToPayment() {
        try {
            Intent intent = new Intent(this, ThanhToan.class);
            Gson gson = new Gson();
            intent.putExtra("product", gson.toJson(product));
            intent.putExtra("quantity", quantity);
            intent.putExtra("size", selectedSize);
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

//    private void selectColor(String color, View colorView) {
//        selectedColor = color;
//
//        // Reset all color selections
//        colorWhite.setSelected(false);
//        colorRed.setSelected(false);
//        colorGray.setSelected(false);
//        colorOrange.setSelected(false);
//        colorLightGray.setSelected(false);
//
//        // Set selected color
//        colorView.setSelected(true);
//    }

    private void selectSize(String size, TextView sizeView) {
        selectedSize = size;

        // Reset all size selections
        resetSizeSelections();

        // Set selected size
        sizeView.setBackgroundResource(R.drawable.size_selector_selected);
        sizeView.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void resetSizeSelections() {
        size37.setBackgroundResource(R.drawable.size_selector);
        size38.setBackgroundResource(R.drawable.size_selector);
        size39.setBackgroundResource(R.drawable.size_selector);
        size40.setBackgroundResource(R.drawable.size_selector);
        size41.setBackgroundResource(R.drawable.size_selector);

        int defaultTextColor = getResources().getColor(R.color.size_text_default);
        size37.setTextColor(defaultTextColor);
        size38.setTextColor(defaultTextColor);
        size39.setTextColor(defaultTextColor);
        size40.setTextColor(defaultTextColor);
        size41.setTextColor(defaultTextColor);
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

        // Ẩn tất cả trước
        size37.setVisibility(View.GONE);
        size38.setVisibility(View.GONE);
        size39.setVisibility(View.GONE);
        size40.setVisibility(View.GONE);
        size41.setVisibility(View.GONE);

        for (Variation variation : product.getVariations()) {
            int size = variation.getSize();
            int stock = variation.getStock();

            if (stock > 0) {
                switch (size) {
                    case 37:
                        size37.setVisibility(View.VISIBLE);
                        break;
                    case 38:
                        size38.setVisibility(View.VISIBLE);
                        break;
                    case 39:
                        size39.setVisibility(View.VISIBLE);
                        break;
                    case 40:
                        size40.setVisibility(View.VISIBLE);
                        break;
                    case 41:
                        size41.setVisibility(View.VISIBLE);
                        break;
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
    private void showAvailableColors() {
        if (product == null || product.getVariations() == null) return;

        layoutColorContainer.removeAllViews();


        Map<String, String> colorMap = new HashMap<>();
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

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setGravity(Gravity.CENTER); // sửa lỗi TEXT_ALIGNMENT_CENTER
            itemLayout.setPadding(16, 0, 16, 0);

            View colorCircle = new View(this);
            int size = getResources().getDimensionPixelSize(R.dimen.color_circle_size);
            LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(size, size);
            circleParams.setMargins(8, 8, 8, 4);
            colorCircle.setLayoutParams(circleParams);
            colorCircle.setBackgroundResource(R.drawable.color_circle_background);
            colorCircle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(code)));


            colorCircle.setTag(code);

            colorCircle.setOnClickListener(v -> {
                String clickedCode = (String) v.getTag();


                if (clickedCode.equals(selectedColorCode)) {
                    selectedColorCode = "";
                    highlightSelectedColor("");
                    showDefaultProductImages();
                } else {
                    selectedColorCode = clickedCode;
                    highlightSelectedColor(clickedCode);
                    updateImageByColor(clickedCode);
                }
            });

            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextSize(12);
            tvName.setTextColor(Color.BLACK);
            tvName.setGravity(Gravity.CENTER);

            itemLayout.addView(colorCircle);
            itemLayout.addView(tvName);
            layoutColorContainer.addView(itemLayout);
        }
    }


    private void highlightSelectedColor(String selectedCode) {
        for (int i = 0; i < layoutColorContainer.getChildCount(); i++) {
            View itemLayout = layoutColorContainer.getChildAt(i);
            if (itemLayout instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) itemLayout;
                View colorCircle = layout.getChildAt(0);
                String tagCode = (String) colorCircle.getTag();

                if (selectedCode != null && selectedCode.equals(tagCode)) {
                    colorCircle.setBackgroundResource(R.drawable.color_circle_selected);
                } else {
                    colorCircle.setBackgroundResource(R.drawable.color_circle_background);
                }
            }
        }
    }



    private void updateImageByColor(String code) {
        if (product == null || product.getVariations() == null) return;

        Set<String> uniqueImages = new LinkedHashSet<>();

        for (Variation v : product.getVariations()) {
            if (v.getColor() != null && code.equalsIgnoreCase(v.getColor().getCode())) {
                Log.d("ImageSlider", "→ matched color: " + code);

                if (v.getImage() != null && !v.getImage().trim().isEmpty()) {
                    uniqueImages.add(ApiClient.IMAGE_URL + v.getImage().trim());
                }

                if (v.getList_imgproduct() != null && !v.getList_imgproduct().isEmpty()) {
                    for (String img : v.getList_imgproduct()) {
                        if (img != null && !img.trim().isEmpty()) {
                            uniqueImages.add(ApiClient.IMAGE_URL + img.trim());
                        }
                    }
                }
            }
        }

        List<String> finalImages;
        if (!uniqueImages.isEmpty()) {
            Log.d("ImageSlider", "Đã tìm thấy ảnh theo màu: " + uniqueImages.size());

            if (uniqueImages.size() == 1) {
                String onlyImage = uniqueImages.iterator().next();
                Log.d("ImageSlider", "Chỉ có 1 ảnh theo màu, đang nhân đôi ảnh: " + onlyImage);
                uniqueImages.add(onlyImage);  // Đảm bảo có ít nhất 2 ảnh
            }

            finalImages = new ArrayList<>(uniqueImages);
        } else {

            Set<String> fallbackImages = new LinkedHashSet<>();
            if (product.getList_imgproduct() != null) {
                for (String img : product.getList_imgproduct()) {
                    if (img != null && !img.trim().isEmpty()) {
                        fallbackImages.add(ApiClient.IMAGE_URL + img.trim());
                    }
                }
            }
            if (product.getAvt_imgproduct() != null && !product.getAvt_imgproduct().trim().isEmpty()) {
                fallbackImages.add(ApiClient.IMAGE_URL + product.getAvt_imgproduct().trim());
            }

            finalImages = new ArrayList<>(fallbackImages);
            if (finalImages.size() == 1) {
                finalImages.add(finalImages.get(0));
            }
        }


        if (imageSliderAdapter != null) {
            imageSliderAdapter.updateImages(finalImages);
        } else {
            imageSliderAdapter = new ImageSliderAdapter(this, finalImages);
            viewPagerProductImages.setAdapter(imageSliderAdapter);
        }

        viewPagerProductImages.setCurrentItem(0, false);
    }


    private void showDefaultProductImages() {
        Set<String> imageSet = new LinkedHashSet<>();

        if (product.getList_imgproduct() != null) {
            for (String img : product.getList_imgproduct()) {
                if (img != null && !img.trim().isEmpty()) {
                    imageSet.add(ApiClient.IMAGE_URL + img.trim());
                }
            }
        }

        if (product.getAvt_imgproduct() != null && !product.getAvt_imgproduct().trim().isEmpty()) {
            imageSet.add(ApiClient.IMAGE_URL + product.getAvt_imgproduct().trim());
        }

        List<String> finalImages = new ArrayList<>(imageSet); // Đã sửa tên biến

        if (!finalImages.isEmpty()) {
            Log.d("ImageSlider", "Hiển thị ảnh mặc định: " + finalImages.size());
            for (String url : finalImages) {
                Log.d("ImageSlider", url);
            }

            // Tạo adapter 1 lần duy nhất
            imageSliderAdapter = new ImageSliderAdapter(this, finalImages);
            viewPagerProductImages.setAdapter(imageSliderAdapter);
            viewPagerProductImages.setCurrentItem(0, false);
        }
    }

}
