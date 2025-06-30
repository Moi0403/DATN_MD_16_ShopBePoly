package com.example.shopbepoly.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.Variation;
import com.example.shopbepoly.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartBottomSheetDialog extends BottomSheetDialogFragment {
    private Context context;
    private Product product;
    private ImageView img;
    private String selectedColorCode = "";
    private String selectedSize = "";
    private String selectedColorName = "";
    private String selectedImageUrl = "";

    private int quantity = 1;

    public CartBottomSheetDialog(Context context, Product product) {
        this.context = context;
        this.product = product;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_cart, container, false);

        LinearLayout layoutColorContainer = view.findViewById(R.id.layoutColorContainer);
        LinearLayout layoutSizeContainer = view.findViewById(R.id.layoutSizeContainer);
        TextView tvQuantity = view.findViewById(R.id.tv_SL);
        TextView tvGia = view.findViewById(R.id.tv_gia_cart);
        TextView tvKho = view.findViewById(R.id.tv_kho_cart);
        ImageView btnDecrease = view.findViewById(R.id.btn_giamSL);
        ImageView btnIncrease = view.findViewById(R.id.btn_tangSL);
        img = view.findViewById(R.id.img_btm_cart);

        Button btnAdd = view.findViewById(R.id.btnAddToCart);


        // Cập nhật giá
        tvGia.setText("Giá: " + String.format("%,d", product.getPrice()) + " đ");

        // Tính tổng kho từ các variations
                int totalStock = 0;
                for (Variation v : product.getVariations()) {
                    totalStock += v.getStock();
                }
                tvKho.setText("Kho: " + totalStock);

        tvQuantity.setText(String.valueOf(quantity));

        Glide.with(context)
                .load(ApiClient.IMAGE_URL + product.getAvt_imgproduct())
                .placeholder(R.drawable.ic_launcher_background) // thêm ảnh chờ
                .error(R.drawable.ic_launcher_foreground) // thêm ảnh lỗi
                .override(300, 300) // giảm độ phân giải để nhẹ
                .centerCrop()
                .into(img);


        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                tvGia.setText(String.format("Giá: " + "%,d đ", quantity * product.getPrice()));
            }
        });

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            tvGia.setText(String.format("Giá: " + "%,d đ", quantity * product.getPrice()));
        });

        // Hiển thị màu
        // Trong onCreateView, sau phần ánh xạ view:
        showColors(layoutColorContainer, layoutSizeContainer, tvKho);
        showSizes(layoutSizeContainer, tvKho);  // truyền thêm kho



        btnAdd.setOnClickListener(v -> {
            if (selectedColorCode.isEmpty() || selectedSize.isEmpty()) {
                Toast.makeText(context, "Vui lòng chọn đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", null);

            if (userId == null) {
                Toast.makeText(context, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            Cart cart = new Cart();
            cart.setIdUser(userId);
            cart.setIdProduct(product);
            cart.setImg_cart(selectedImageUrl);
            cart.setPrice(product.getPrice());
            cart.setQuantity(quantity);
            cart.setTotal(product.getPrice() * quantity);
            cart.setSize(Integer.parseInt(selectedSize));
            cart.setColor(selectedColorName);
            cart.setStatus(0);

            ApiService apiService = ApiClient.getApiService();
            apiService.addCart(cart).enqueue(new Callback<Cart>() {
                @Override
                public void onResponse(Call<Cart> call, Response<Cart> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        Log.d("CART_DATA", new Gson().toJson(cart));
                        dismiss();
                    } else {
                        Toast.makeText(context, "Thêm giỏ hàng thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Cart> call, Throwable t) {
                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    private void showColors(LinearLayout layoutColorContainer, LinearLayout layoutSizeContainer, TextView tvKho) {
        layoutColorContainer.removeAllViews();
        Set<String> added = new HashSet<>();
        boolean first = true;

        for (Variation v : product.getVariations()) {
            if (v.getColor() != null) {
                String code = v.getColor().getCode();
                String name = v.getColor().getName();

                if (!added.contains(code)) {
                    added.add(code);

                    LinearLayout itemLayout = new LinearLayout(context);
                    itemLayout.setOrientation(LinearLayout.VERTICAL);
                    itemLayout.setPadding(16, 0, 16, 0);
                    itemLayout.setGravity(Gravity.CENTER);

                    View colorCircle = new View(context);
                    int sizePx = getResources().getDimensionPixelSize(R.dimen.color_circle_size);
                    LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(sizePx, sizePx);
                    circleParams.setMargins(8, 8, 8, 4);
                    colorCircle.setLayoutParams(circleParams);
                    colorCircle.setTag(code);

                    colorCircle.setBackgroundResource(R.drawable.color_circle_background);
                    colorCircle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(code)));

                    colorCircle.setOnClickListener(view -> {
                        selectedColorCode = code;
                        selectedColorName = name;
                        highlightSelectedColor(layoutColorContainer, selectedColorCode);
                        autoSelectFirstValidSize(layoutSizeContainer, tvKho);
                        updateImageByColor(); // <- Cập nhật ảnh theo màu
                    });


                    TextView tvName = new TextView(context);
                    tvName.setText(name);
                    tvName.setTextSize(12);
                    tvName.setTextColor(Color.BLACK);
                    tvName.setGravity(Gravity.CENTER);

                    itemLayout.addView(colorCircle);
                    itemLayout.addView(tvName);
                    layoutColorContainer.addView(itemLayout);

                    if (first) {
                        first = false;
                        colorCircle.performClick();
                    }
                }
            }
        }
    }



    private void highlightSelectedColor(LinearLayout container, String selected) {
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout layout = (LinearLayout) container.getChildAt(i);
            View circle = layout.getChildAt(0); // View là colorCircle
            String code = (String) circle.getTag();

            if (selected.equals(code)) {
                circle.setBackgroundResource(R.drawable.color_circle_selected);
            } else {
                circle.setBackgroundResource(R.drawable.color_circle_background);
            }

            // Sau khi đặt background, cần set lại màu:
            circle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(code)));
        }
    }



    private void showSizes(LinearLayout layoutSizeContainer, TextView tvKho) {
        layoutSizeContainer.removeAllViews();
        Set<Integer> added = new HashSet<>();
        boolean first = true;

        // 🔽 Lọc các variation theo màu đã chọn
        List<Variation> filtered = new ArrayList<>();
        for (Variation v : product.getVariations()) {
            if (v.getColor() != null && v.getColor().getCode().equals(selectedColorCode)) {
                filtered.add(v);
            }
        }

        // ✅ Sắp xếp theo size tăng dần
        Collections.sort(filtered, Comparator.comparingInt(Variation::getSize));

        for (Variation v : filtered) {
            int size = v.getSize();
            int stock = v.getStock();

            if (stock > 0 && !added.contains(size)) {
                added.add(size);

                TextView sizeView = new TextView(context);
                sizeView.setText(String.valueOf(size));
                sizeView.setPadding(24, 16, 24, 16);
                sizeView.setBackgroundResource(R.drawable.size_selector);
                sizeView.setTextColor(ContextCompat.getColor(context, R.color.size_text_default));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(12, 0, 12, 0);
                sizeView.setLayoutParams(params);

                sizeView.setOnClickListener(view -> {
                    selectedSize = String.valueOf(size);
                    highlightSelectedSize(layoutSizeContainer, sizeView);
                    updateStockForSelection(tvKho);
                });

                layoutSizeContainer.addView(sizeView);

                if (first) {
                    first = false;
                    sizeView.performClick(); // tự chọn size đầu tiên
                }
            }
        }   
    }




    private void highlightSelectedSize(LinearLayout container, TextView selectedView) {
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView tv = (TextView) container.getChildAt(i);
            tv.setBackgroundResource(R.drawable.size_selector);
            tv.setTextColor(ContextCompat.getColor(context, R.color.size_text_default));
        }

        selectedView.setBackgroundResource(R.drawable.size_selector_selected);
        selectedView.setTextColor(Color.WHITE);
    }
    private void updateStockForSelection(TextView tvKho) {
        for (Variation v : product.getVariations()) {
            if (v.getColor() != null && v.getColor().getCode().equals(selectedColorCode)
                    && String.valueOf(v.getSize()).equals(selectedSize)) {
                tvKho.setText("Kho: " + v.getStock());
                return;
            }
        }
        tvKho.setText("Kho: 0"); // nếu không tìm thấy
    }
    private void autoSelectFirstValidSize(LinearLayout layoutSizeContainer, TextView tvKho) {
        selectedSize = "";
        showSizes(layoutSizeContainer, tvKho);
    }

    private void updateImageByColor() {
        for (Variation v : product.getVariations()) {
            if (v.getColor() != null && v.getColor().getCode().equals(selectedColorCode)) {
                if (v.getList_imgproduct() != null && !v.getList_imgproduct().isEmpty()) {
                    selectedImageUrl = ApiClient.IMAGE_URL + v.getList_imgproduct().get(0); // Lưu lại
                    Glide.with(context)
                            .load(selectedImageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_foreground)
                            .override(300, 300)
                            .centerCrop()
                            .into(img);
                    return;
                }
            }
        }

        // Fallback
        selectedImageUrl = ApiClient.IMAGE_URL + product.getAvt_imgproduct(); // Lưu lại ảnh mặc định
        Glide.with(context)
                .load(selectedImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .override(300, 300)
                .centerCrop()
                .into(img);
    }



}

