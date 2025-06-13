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
import com.example.shopbepoly.R;
import com.example.shopbepoly.fragment.FavoriteFragment;
import com.squareup.picasso.Picasso;

public class ChiTietSanPham extends AppCompatActivity {

    private ImageView btnBack, btnFavorite, btnDecrease, btnIncrease,imgProduct;
    private TextView tvQuantity, tvProductName, tvPrice, tvDescription;
    private AppCompatButton btnAddToCart;
    private View colorWhite, colorRed, colorGray, colorOrange, colorLightGray;
    private TextView size35, size38, size40, size45;

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
        size35 = findViewById(R.id.size35);
        size38 = findViewById(R.id.size38);
        size40 = findViewById(R.id.size40);
        size45 = findViewById(R.id.size45);
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
        size35.setOnClickListener(v -> selectSize("35", size35));
        size38.setOnClickListener(v -> selectSize("38", size38));
        size40.setOnClickListener(v -> selectSize("40", size40));
        size45.setOnClickListener(v -> selectSize("45", size45));
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
        size35.setBackgroundResource(R.drawable.size_selector);
        size38.setBackgroundResource(R.drawable.size_selector);
        size40.setBackgroundResource(R.drawable.size_selector);
        size45.setBackgroundResource(R.drawable.size_selector);

        int defaultTextColor = getResources().getColor(R.color.size_text_default);
        size35.setTextColor(defaultTextColor);
        size38.setTextColor(defaultTextColor);
        size40.setTextColor(defaultTextColor);
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

    private void updateUI() {
        updateQuantity();
        updateFavoriteButton();

//        // Set default selections
//        colorWhite.setSelected(true);
    }

}
