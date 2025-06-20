package com.example.shopbepoly.Screen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Favorite;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.Variation;
import com.example.shopbepoly.R;
import com.example.shopbepoly.fragment.FavoriteFragment;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChiTietSanPham extends AppCompatActivity {

    private ImageView btnBack, btnFavorite, btnDecrease, btnIncrease,imgProduct, btnCart;
    private TextView tvQuantity, tvProductName, tvPrice, tvDescription;
    private AppCompatButton btnAddToCart;
    private View colorWhite, colorRed, colorGray, colorOrange, colorLightGray;
    private TextView size37, size38, size39, size40,size41;

    private int quantity = 1;
    private boolean isFavorite = true;
    private String selectedColor = "white";
    private String selectedSize = "40";
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_san_pham);
        btnCart = findViewById(R.id.btnCart);

        initViews();
        setupClickListeners();

        product = (Product) getIntent().getSerializableExtra("product");
        if (product!= null){
            tvProductName.setText(product.getNameproduct());
            tvPrice.setText(String.valueOf(product.getPrice()));
            tvDescription.setText(product.getDescription());

            Picasso.get().load(ApiClient.IMAGE_URL + product.getAvt_imgproduct()).into(imgProduct);
            isFavorite = FavoriteFragment.isFavorite(product);
            updateFavoriteButton();

            showAvailableSizes();
        }
        updateUI();

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = ChiTietSanPham.this.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
                String userId = sharedPreferences.getString("userId", null);

                if (userId == null) {
                    Toast.makeText(ChiTietSanPham.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    return;
                }
                Cart cart = new Cart();
                cart.setIdUser(userId);
                cart.setIdProduct(product);
                cart.setQuantity(quantity);
                cart.setPrice(product.getPrice());
                cart.setTotal(product.getPrice() * quantity);
                cart.setSize(Integer.parseInt(String.valueOf(selectedSize)));
                cart.setStatus(0);
                Add_Cart(cart);
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
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        imgProduct = findViewById(R.id.imgProduct);

//        // Color selectors
//        colorWhite = findViewById(R.id.colorWhite);
//        colorRed = findViewById(R.id.colorRed);
//        colorGray = findViewById(R.id.colorGray);
//        colorOrange = findViewById(R.id.colorOrange);
//        colorLightGray = findViewById(R.id.colorLightGray);

        // Size selectors
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
                api.removeFavorite(userId,product.get_id()).enqueue(new Callback<ResponseBody>() {
                    @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                        if (r.isSuccessful()) {
                            isFavorite = false;
                            FavoriteFragment.remove(getApplicationContext(),product);
                            updateFavoriteButton();
                            Toast.makeText(ChiTietSanPham.this, "Xóa sản phẩm yêu thích thành công!", Toast.LENGTH_SHORT).show();
                        }
                        btnFavorite.setEnabled(true);
                    }
                    @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                        btnFavorite.setEnabled(true);
                    }
                });
            } else {
                api.addFavorite(fav).enqueue(new Callback<Favorite>() {
                    @Override public void onResponse(Call<Favorite> c, Response<Favorite> r) {
                        if (r.isSuccessful()) {
                            isFavorite = true;
                            FavoriteFragment.add(getApplicationContext(),product);
                            updateFavoriteButton();
                            Toast.makeText(ChiTietSanPham.this, "Đã thêm sản phẩm vào yêu thích!", Toast.LENGTH_SHORT).show();
                        }
                        btnFavorite.setEnabled(true);
                    }
                    @Override public void onFailure(Call<Favorite> c, Throwable t) {
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
            Toast.makeText(this, "Đã thêm " + quantity + " sản phẩm vào giỏ hàng!",
                    Toast.LENGTH_SHORT).show();
        });

//        // Color selection
//        colorWhite.setOnClickListener(v -> selectColor("white", colorWhite));
//        colorRed.setOnClickListener(v -> selectColor("red", colorRed));
//        colorGray.setOnClickListener(v -> selectColor("gray", colorGray));
//        colorOrange.setOnClickListener(v -> selectColor("orange", colorOrange));
//        colorLightGray.setOnClickListener(v -> selectColor("lightgray", colorLightGray));

        // Size selection
        size37.setOnClickListener(v -> selectSize("37", size37));
        size38.setOnClickListener(v -> selectSize("38", size38));
        size39.setOnClickListener(v -> selectSize("39", size39));
        size40.setOnClickListener(v -> selectSize("40", size40));
        size41.setOnClickListener(v -> selectSize("41", size41));
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

//        // Set default selections
//        colorWhite.setSelected(true);
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

}
