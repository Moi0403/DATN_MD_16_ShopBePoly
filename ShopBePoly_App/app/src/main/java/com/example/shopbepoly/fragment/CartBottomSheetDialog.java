package com.example.shopbepoly.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.CartAdapter;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.ResponseBody;
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
    private CartUpdateListener updateListener;
    private String editingCartId = null;

    public interface CartUpdateListener {
        void onCartItemAdded(Cart newCartItem);
        void onCartUpdated();
    }

    public CartBottomSheetDialog(Context context, Product product, CartUpdateListener updateListener, String editingCartId) {
        this.context = context;
        this.product = product;
        this.updateListener = updateListener;
        this.editingCartId = editingCartId;
    }

    public CartBottomSheetDialog(Context context, Product product) {
        this(context, product, null, null);
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
        TextView tvTen = view.findViewById(R.id.tv_ten_cart);
        TextView tv_cate_product = view.findViewById(R.id.tv_cate_cart);
        ImageView btnDecrease = view.findViewById(R.id.btn_giamSL);
        ImageView btnIncrease = view.findViewById(R.id.btn_tangSL);
        img = view.findViewById(R.id.img_btm_cart);
        Button btnAdd = view.findViewById(R.id.btnAddToCart);

        int originalPrice = product.getPrice();
        int salePrice = product.getPrice_sale();
        int discount = product.getSale();

        // Display price with proper formatting
        updatePriceDisplay(tvGia, quantity);

        tvTen.setText(product.getNameproduct());
        tvQuantity.setText(String.valueOf(quantity));
        tv_cate_product.setText("(" + product.getCategoryName() + ")");

        // Initialize stock display
        updateStockDisplay(tvKho);

        Glide.with(context)
                .load(ApiClient.IMAGE_URL + product.getAvt_imgproduct())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .override(300, 300)
                .centerCrop()
                .into(img);

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updatePriceDisplay(tvGia, quantity);
            }
        });

        btnIncrease.setOnClickListener(v -> {
            int maxStock = getCurrentStock();

            if (selectedColorCode.isEmpty() || selectedSize.isEmpty()) {
                Toast.makeText(context, "Vui lòng chọn màu và size trước", Toast.LENGTH_SHORT).show();
                return;
            }

            if (quantity < maxStock) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
                updatePriceDisplay(tvGia, quantity);
            } else {
                Toast.makeText(context, "Vượt quá số lượng trong kho (" + maxStock + ")", Toast.LENGTH_SHORT).show();
            }
        });

        if (editingCartId != null) {
            // Editing existing cart item
            SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", null);

            btnAdd.setText("Cập nhật đơn hàng");

            if (userId != null) {
                loadExistingCartData(userId, layoutColorContainer, layoutSizeContainer, tvKho, tvQuantity, tvGia);
            }
        } else {
            showAvailableColors(layoutColorContainer, layoutSizeContainer, tvKho);
            showSizes(layoutSizeContainer, tvKho, true);
        }

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateSelection()) {
                    return;
                }

                SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
                String userId = sharedPreferences.getString("userId", null);
                if (userId == null) {
                    Toast.makeText(context, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check stock before adding/updating
                int availableStock = getCurrentStock();
                if (quantity > availableStock) {
                    Toast.makeText(context, "Số lượng vượt quá kho (" + availableStock + ")", Toast.LENGTH_SHORT).show();
                    return;
                }

                processCartOperation(userId);
            }
        });

        return view;
    }

    // PHƯƠNG THỨC CHỌN MÀU ĐƯỢC CẢI THIỆN GIỐNG ChiTietSanPham
    private void showAvailableColors(LinearLayout layoutColorContainer, LinearLayout layoutSizeContainer, TextView tvKho) {
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
            LinearLayout itemLayout = new LinearLayout(context);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setGravity(Gravity.CENTER);
            itemLayout.setPadding(16, 8, 16, 8);

            // Container cho color circle với shadow effect
            FrameLayout colorContainer = new FrameLayout(context);
            int containerSize = getResources().getDimensionPixelSize(R.dimen.color_circle_size) + 20;
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(containerSize, containerSize);
            containerParams.setMargins(8, 8, 8, 4);
            colorContainer.setLayoutParams(containerParams);

            // Shadow view (tạo hiệu ứng đổ bóng)
            View shadowView = new View(context);
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
            View colorCircle = new View(context);
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
            ImageView checkMark = new ImageView(context);
            FrameLayout.LayoutParams checkParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            checkParams.gravity = Gravity.CENTER;
            checkMark.setLayoutParams(checkParams);
            checkMark.setImageResource(R.drawable.ic_check_white); // Thêm icon checkmark
            checkMark.setVisibility(View.GONE);
            checkMark.setTag("checkmark_" + code);

            // Text tên màu với styling
            TextView tvName = new TextView(context);
            tvName.setText(name);
            tvName.setTextSize(12);
            tvName.setTextColor(Color.BLACK);
            tvName.setGravity(Gravity.CENTER);
            tvName.setTypeface(null, android.graphics.Typeface.NORMAL);
            tvName.setPadding(4, 4, 4, 4);

            // Sự kiện click cho color circle
            colorCircle.setOnClickListener(v -> {
                String clickedCode = (String) v.getTag();
                handleColorSelection(clickedCode, name, layoutColorContainer, layoutSizeContainer, tvKho);
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

    // Phương thức xử lý chọn màu được cải thiện
    private void handleColorSelection(String clickedCode, String colorName, LinearLayout layoutColorContainer,
                                      LinearLayout layoutSizeContainer, TextView tvKho) {
        if (clickedCode.equals(selectedColorCode)) {
            // Bỏ chọn màu hiện tại
            selectedColorCode = "";
            selectedColorName = "";
            selectedSize = "";
            highlightSelectedColor(layoutColorContainer, "");
            updateImageByColor(); // Show default image
            resetSizeSelections(layoutSizeContainer);
        } else {
            // Chọn màu mới
            selectedColorCode = clickedCode;
            selectedColorName = colorName;
            selectedSize = ""; // Reset size when selecting new color
            highlightSelectedColor(layoutColorContainer, clickedCode);
            updateImageByColor();
            resetSizeSelections(layoutSizeContainer);
        }

        showSizes(layoutSizeContainer, tvKho, true);
        updateStockDisplay(tvKho);
    }

    // Phương thức highlight được cải thiện giống ChiTietSanPham
    private void highlightSelectedColor(LinearLayout layoutColorContainer, String selectedCode) {
        for (int i = 0; i < layoutColorContainer.getChildCount(); i++) {
            LinearLayout itemLayout = (LinearLayout) layoutColorContainer.getChildAt(i);
            if (itemLayout.getChildCount() >= 2) {
                FrameLayout colorContainer = (FrameLayout) itemLayout.getChildAt(0);
                TextView nameText = (TextView) itemLayout.getChildAt(1);

                if (colorContainer.getChildCount() >= 3) {
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
    }

    private void resetSizeSelections(LinearLayout layoutSizeContainer) {
        for (int i = 0; i < layoutSizeContainer.getChildCount(); i++) {
            TextView sizeView = (TextView) layoutSizeContainer.getChildAt(i);
            sizeView.setBackgroundResource(R.drawable.size_selector);
            sizeView.setTextColor(ContextCompat.getColor(context, R.color.size_text_default));
        }
    }

    private void updateStockDisplay(TextView tvKho) {
        if (product == null || product.getVariations() == null) {
            tvKho.setText("Kho: 0");
            return;
        }

        // Case 1: No color selected - show total stock
        if (selectedColorCode.isEmpty()) {
            int totalStock = 0;
            for (Variation variation : product.getVariations()) {
                totalStock += variation.getStock();
            }
            tvKho.setText("Kho: " + totalStock);
            return;
        }

        // Case 2: Color selected but no size - show stock for that color
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

        // Case 3: Both color and size selected - show specific variation stock
        for (Variation variation : product.getVariations()) {
            if (variation.getSize() == Integer.parseInt(selectedSize) &&
                    variation.getColor() != null &&
                    selectedColorCode.equalsIgnoreCase(variation.getColor().getCode())) {
                tvKho.setText("Kho: " + variation.getStock() + " (Màu " + selectedColorName + ", Size " + selectedSize + ")");
                return;
            }
        }

        // Case 4: No matching variation found
        tvKho.setText("Kho: 0 (Hết hàng)");
    }

    private boolean validateSelection() {
        if (selectedColorName == null || selectedColorName.isEmpty()) {
            Toast.makeText(context, "Vui lòng chọn màu", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedSize == null || selectedSize.isEmpty()) {
            Toast.makeText(context, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (quantity <= 0) {
            Toast.makeText(context, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void loadExistingCartData(String userId, LinearLayout layoutColorContainer,
                                      LinearLayout layoutSizeContainer, TextView tvKho,
                                      TextView tvQuantity, TextView tvGia) {
        ApiService apiService = ApiClient.getApiService();
        apiService.getCart(userId).enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Cart c : response.body()) {
                        if (c.get_id().equals(editingCartId)) {
                            selectedColorName = c.getColor();
                            selectedSize = String.valueOf(c.getSize());
                            quantity = c.getQuantity();

                            // Find color code
                            for (Variation v : product.getVariations()) {
                                if (v.getColor() != null && v.getColor().getName().equals(selectedColorName)) {
                                    selectedColorCode = v.getColor().getCode();
                                    break;
                                }
                            }

                            // Update UI
                            tvQuantity.setText(String.valueOf(quantity));
                            updatePriceDisplay(tvGia, quantity);

                            showAvailableColors(layoutColorContainer, layoutSizeContainer, tvKho);
                            highlightSelectedColor(layoutColorContainer, selectedColorCode);
                            showSizes(layoutSizeContainer, tvKho, false);
                            updateStockDisplay(tvKho);
                            highlightSelectedSizeInContainer(layoutSizeContainer);
                            updateImageByColor();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                Log.e("CartBottomSheet", "Failed to load cart data", t);
            }
        });
    }

    private void updatePriceDisplay(TextView tvGia, int quantity) {
        int originalPrice = product.getPrice();
        int salePrice = product.getPrice_sale();
        int discount = product.getSale();

        if (discount > 0 && salePrice > 0 && salePrice < originalPrice) {
            int totalOriginal = originalPrice * quantity;
            int totalSale = salePrice * quantity;

            SpannableString finalPriceStr = new SpannableString(String.format("%,d đ", totalSale));
            finalPriceStr.setSpan(
                    new ForegroundColorSpan(getResources().getColor(R.color.heart_color)),
                    0,
                    finalPriceStr.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            SpannableString originalPriceStr = new SpannableString(String.format("%,d đ", totalOriginal));
            originalPriceStr.setSpan(new StrikethroughSpan(), 0, originalPriceStr.length(), 0);
            originalPriceStr.setSpan(
                    new ForegroundColorSpan(getResources().getColor(android.R.color.darker_gray)),
                    0,
                    originalPriceStr.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            originalPriceStr.setSpan(
                    new android.text.style.RelativeSizeSpan(0.8f),
                    0,
                    originalPriceStr.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            tvGia.setText(TextUtils.concat("Giá: ", finalPriceStr, "  ", originalPriceStr));
        } else {
            int total = originalPrice * quantity;
            tvGia.setText("Giá: " + String.format("%,d", total) + " đ");
            tvGia.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void showSizes(LinearLayout layoutSizeContainer, TextView tvKho, boolean autoSelect) {
        layoutSizeContainer.removeAllViews();

        if (selectedColorCode.isEmpty()) {
            updateStockDisplay(tvKho);
            return;
        }

        Set<Integer> added = new HashSet<>();
        List<Variation> filtered = new ArrayList<>();

        for (Variation v : product.getVariations()) {
            if (v.getColor() != null &&
                    v.getColor().getCode().equals(selectedColorCode) &&
                    v.getStock() > 0) { // Only show sizes with stock
                filtered.add(v);
            }
        }

        Collections.sort(filtered, Comparator.comparingInt(Variation::getSize));

        for (Variation v : filtered) {
            int size = v.getSize();

            if (!added.contains(size)) {
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
                    updateStockDisplay(tvKho);
                });

                layoutSizeContainer.addView(sizeView);

                // Auto-select first size if needed
                if (autoSelect && selectedSize.isEmpty() && layoutSizeContainer.getChildCount() == 1) {
                    sizeView.performClick();
                }
            }
        }

        updateStockDisplay(tvKho);
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

    private void highlightSelectedSizeInContainer(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView sizeView = (TextView) container.getChildAt(i);
            if (sizeView.getText().toString().equals(selectedSize)) {
                highlightSelectedSize(container, sizeView);
                break;
            }
        }
    }

    private void updateImageByColor() {
        if (selectedColorCode.isEmpty()) {
            // Show default image
            Glide.with(context)
                    .load(ApiClient.IMAGE_URL + product.getAvt_imgproduct())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .override(300, 300)
                    .centerCrop()
                    .into(img);
            return;
        }

        // Find image for selected color
        for (Variation v : product.getVariations()) {
            if (v.getColor() != null && v.getColor().getCode().equals(selectedColorCode)) {
                String imageUrl = null;

                // Try variation image first
                if (v.getImage() != null && !v.getImage().trim().isEmpty()) {
                    imageUrl = ApiClient.IMAGE_URL + v.getImage().trim();
                }
                // Then try list images
                else if (v.getList_imgproduct() != null && !v.getList_imgproduct().isEmpty()) {
                    imageUrl = ApiClient.IMAGE_URL + v.getList_imgproduct().get(0);
                }

                if (imageUrl != null) {
                    selectedImageUrl = imageUrl;
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

        // Fallback to default image
        selectedImageUrl = ApiClient.IMAGE_URL + product.getAvt_imgproduct();
        Glide.with(context)
                .load(selectedImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .override(300, 300)
                .centerCrop()
                .into(img);
    }

    private int getCurrentStock() {
        if (selectedColorCode.isEmpty() || selectedSize.isEmpty()) {
            return 0;
        }

        for (Variation v : product.getVariations()) {
            if (v.getColor() != null &&
                    v.getColor().getCode().equals(selectedColorCode) &&
                    String.valueOf(v.getSize()).equals(selectedSize)) {
                return v.getStock();
            }
        }
        return 0;
    }

    private void processCartOperation(String userId) {
        String selectedImageUrl = product.getAvt_imgproduct();
        int selectedPrice = (product.getPrice_sale() > 0) ? product.getPrice_sale() : product.getPrice();

        // Find the exact variation to get image and price
        for (Variation variant : product.getVariations()) {
            if (variant.getColor() != null &&
                    variant.getColor().getName().equals(selectedColorName) &&
                    String.valueOf(variant.getSize()).equals(selectedSize)) {

                if (variant.getList_imgproduct() != null && !variant.getList_imgproduct().isEmpty()) {
                    selectedImageUrl = ApiClient.IMAGE_URL + variant.getList_imgproduct().get(0);
                }
                break;
            }
        }

        ApiService apiService = ApiClient.getApiService();
        String finalSelectedImageUrl = selectedImageUrl;

        apiService.getCart(userId).enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful()) {
                    List<Cart> cartList = response.body();
                    Cart matchingCart = null;

                    // Find matching cart item
                    for (Cart cartItem : cartList) {
                        if (cartItem.getIdProduct().get_id().equals(product.get_id())
                                && cartItem.getColor().equals(selectedColorName)
                                && cartItem.getSize() == Integer.parseInt(selectedSize)
                                && (editingCartId == null || !cartItem.get_id().equals(editingCartId))) {
                            matchingCart = cartItem;
                            break;
                        }
                    }

                    if (editingCartId != null) {
                        handleEditCart(apiService, matchingCart, selectedPrice, finalSelectedImageUrl, userId);
                    } else {
                        handleAddCart(apiService, matchingCart, selectedPrice, finalSelectedImageUrl, userId);
                    }
                } else {
                    Toast.makeText(context, "Không lấy được giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleEditCart(ApiService apiService, Cart matchingCart, int selectedPrice, String finalSelectedImageUrl, String userId) {
        if (matchingCart != null) {
            // Merge with existing cart item
            int newQuantity = matchingCart.getQuantity() + quantity;
            int maxStock = getCurrentStock();

            if (newQuantity > maxStock) {
                Toast.makeText(context, "Tổng số lượng vượt quá kho (" + maxStock + ")", Toast.LENGTH_SHORT).show();
                return;
            }

            matchingCart.setQuantity(newQuantity);
            matchingCart.setTotal(selectedPrice * newQuantity);

            apiService.upCart(matchingCart.get_id(), matchingCart).enqueue(new Callback<Cart>() {
                @Override
                public void onResponse(Call<Cart> call, Response<Cart> response) {
                    if (response.isSuccessful()) {
                        apiService.delCart(editingCartId).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(context, "Đã gộp và cập nhật đơn hàng", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                    if (updateListener != null) updateListener.onCartUpdated();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Lỗi khi xóa đơn hàng cũ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<Cart> call, Throwable t) {
                    Toast.makeText(context, "Lỗi khi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Update existing cart item
            Cart updatedCart = createCartItem(userId, selectedPrice, finalSelectedImageUrl);

            apiService.upCart(editingCartId, updatedCart).enqueue(new Callback<Cart>() {
                @Override
                public void onResponse(Call<Cart> call, Response<Cart> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Đã cập nhật đơn hàng", Toast.LENGTH_SHORT).show();
                        dismiss();
                        if (updateListener != null) updateListener.onCartUpdated();
                    }
                }

                @Override
                public void onFailure(Call<Cart> call, Throwable t) {
                    Toast.makeText(context, "Lỗi khi cập nhật đơn hàng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleAddCart(ApiService apiService, Cart matchingCart, int selectedPrice, String finalSelectedImageUrl, String userId) {
        if (matchingCart != null) {
            // Update existing cart item
            int newQuantity = matchingCart.getQuantity() + quantity;
            int maxStock = getCurrentStock();

            if (newQuantity > maxStock) {
                Toast.makeText(context, "Tổng số lượng vượt quá kho (" + maxStock + ")", Toast.LENGTH_SHORT).show();
                return;
            }

            matchingCart.setQuantity(newQuantity);
            matchingCart.setTotal(selectedPrice * newQuantity);

            apiService.upCart(matchingCart.get_id(), matchingCart).enqueue(new Callback<Cart>() {
                @Override
                public void onResponse(Call<Cart> call, Response<Cart> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Đã cập nhật số lượng", Toast.LENGTH_SHORT).show();
                        dismiss();
                        if (updateListener != null) updateListener.onCartUpdated();
                    }
                }

                @Override
                public void onFailure(Call<Cart> call, Throwable t) {
                    Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Add new cart item
            Cart newCart = createCartItem(userId, selectedPrice, finalSelectedImageUrl);

            apiService.addCart(newCart).enqueue(new Callback<Cart>() {
                @Override
                public void onResponse(Call<Cart> call, Response<Cart> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        dismiss();
                        if (updateListener != null) updateListener.onCartUpdated();
                    }
                }

                @Override
                public void onFailure(Call<Cart> call, Throwable t) {
                    Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Cart createCartItem(String userId, int selectedPrice, String finalSelectedImageUrl) {
        Cart cart = new Cart();
        cart.setIdUser(userId);
        cart.setIdProduct(product);
        cart.setColor(selectedColorName);
        cart.setSize(Integer.parseInt(selectedSize));
        cart.setQuantity(quantity);
        cart.setImg_cart(finalSelectedImageUrl);
        cart.setPrice(selectedPrice);
        cart.setTotal(selectedPrice * quantity);

        return cart;
    }
}