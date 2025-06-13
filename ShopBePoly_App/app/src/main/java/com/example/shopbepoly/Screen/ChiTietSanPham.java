package com.example.shopbepoly.Screen;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.Variation;
import com.example.shopbepoly.R;
import com.example.shopbepoly.fragment.FavoriteFragment;
import com.squareup.picasso.Picasso;

public class ChiTietSanPham extends AppCompatActivity {

    private ImageView btnBack, btnFavorite, btnDecrease, btnIncrease,imgProduct;
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

            if (isFavorite) {
                FavoriteFragment.remove(product);
            } else {
                FavoriteFragment.add(product);
            }

            isFavorite = !isFavorite;
            updateFavoriteButton();
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

}
