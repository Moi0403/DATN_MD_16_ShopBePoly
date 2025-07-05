package com.example.shopbepoly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.Address;
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.User;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThanhToan extends AppCompatActivity {

    private static final String TAG = "ThanhToan";

    private Product selectedProduct;
    private User currentUser;
    private int quantity = 1;
    private int productPrice = 0;
    private int shippingFee = 30000;
    private String selectedSize = "", selectedColor = "", userId;

    private TextView txtProductName, txtProductColor, txtProductQuantity, txtProductSize, txtProductPrice,
            txtProductTotal, txtShippingFee, txtTotalPayment, txtCustomerName, txtCustomerAddress, txtCustomerPhone, txtShippingNote;
    private ImageView imgProduct, img_next_address;
    private RadioGroup radioGroupShipping;
    private RadioButton radioStandardShipping, radioFastShipping;
    private Button btnDatHang;

    private static final int REQ_ADDRESS = 3001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);

        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");


        initViews();
        loadUserInfo();
        radioStandardShipping.setChecked(true);

        getDataFromIntent();

        selectedColor = getIntent().getStringExtra("color");
        setupListeners();

        if (selectedProduct != null) displayProductInfo();

        String jsonCart = getIntent().getStringExtra("cart_list");
        if (jsonCart != null && !jsonCart.isEmpty()) {
            List<Cart> cartList = new Gson().fromJson(jsonCart, new com.google.gson.reflect.TypeToken<List<Cart>>() {}.getType());
            RecyclerView recyclerView = findViewById(R.id.recyclerView_cart_items);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new com.example.shopbepoly.Adapter.PayAdapter(this, cartList));
            calculateTotalFromCart(cartList);
        }

        btnDatHang.setOnClickListener(v -> {
            String name = txtCustomerName.getText().toString().trim();
            String phone = txtCustomerPhone.getText().toString().trim();
            String address = txtCustomerAddress.getText().toString().trim();

            int selectedPaymentId = ((RadioGroup) findViewById(R.id.radioGroupPaymentMain)).getCheckedRadioButtonId();
            int selectedBankId = ((RadioGroup) findViewById(R.id.radioGroupBank)).getCheckedRadioButtonId();

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin khách hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedPaymentId == -1) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedPaymentId == R.id.radioAppBank && selectedBankId == -1) {
                Toast.makeText(this, "Vui lòng chọn ngân hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            startActivity(new Intent(ThanhToan.this, Dathangthanhcong.class));
            finish();
        });
    }

    private void initViews() {
        imgProduct = findViewById(R.id.imgProduct);
        txtProductName = findViewById(R.id.txtProductName);
        txtProductQuantity = findViewById(R.id.txtProductQuantity);
        txtProductColor = findViewById(R.id.txtProductColor);
        txtProductSize = findViewById(R.id.txtProductSize);
        txtProductPrice = findViewById(R.id.txtProductPrice);
        txtProductTotal = findViewById(R.id.txtProductTotal);
        txtShippingFee = findViewById(R.id.txtShippingFee);
        txtTotalPayment = findViewById(R.id.txtTotalPayment);
        txtShippingNote = findViewById(R.id.txtShippingNote); // thêm vào XML layout nếu chưa có
        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtCustomerAddress = findViewById(R.id.txtCustomerAddress);
        txtCustomerPhone = findViewById(R.id.txtCustomerPhone);
        img_next_address = findViewById(R.id.img_next_Adress);
        radioGroupShipping = findViewById(R.id.radioGroupShipping);
        radioStandardShipping = findViewById(R.id.radioStandardShipping);
        radioFastShipping = findViewById(R.id.radioFastShipping);
        btnDatHang = findViewById(R.id.btnDatHang);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        radioGroupShipping.setOnCheckedChangeListener((group, checkedId) -> updateShippingFeeBasedOnAddress());

        ((RadioGroup) findViewById(R.id.radioGroupPaymentMain)).setOnCheckedChangeListener((group, checkedId) -> {
            View layoutBankOptions = findViewById(R.id.layoutBankOptions);
            if (checkedId == R.id.radioAppBank) {
                layoutBankOptions.setVisibility(View.VISIBLE);
            } else {
                layoutBankOptions.setVisibility(View.GONE);
                ((RadioGroup) findViewById(R.id.radioGroupBank)).clearCheck();
            }
        });

        img_next_address.setOnClickListener(v -> startActivityForResult(new Intent(this, AddressListActivity.class), REQ_ADDRESS));
    }

    private void getDataFromIntent() {
        String productJson = getIntent().getStringExtra("product");
        if (productJson != null) {
            selectedProduct = new Gson().fromJson(productJson, Product.class);
            quantity = getIntent().getIntExtra("quantity", 1);
            selectedSize = getIntent().getStringExtra("size");
        }
    }

    private void loadUserInfo() {
        ApiClient.getApiService().getUsers().enqueue(new Callback<List<User>>() {
            @Override public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    for (User user : response.body()) {
                        if (user.getId().equals(userId)) {
                            currentUser = user;
                            displayUserInfo();
                            updateShippingFeeBasedOnAddress();
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(ThanhToan.this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserInfo() {
        SharedPreferences prefs = getSharedPreferences("AddressPrefs", MODE_PRIVATE);
        String json = prefs.getString("default_address_" + userId, "");
        if (!json.isEmpty()) {
            try {
                Address address = new Gson().fromJson(json, Address.class);
                if (address != null) {
                    txtCustomerName.setText(address.getName());
                    txtCustomerPhone.setText(address.getPhone());
                    txtCustomerAddress.setText(address.getAddress());
                    return; // nếu đã có địa chỉ mặc định thì không cần hiển thị từ user nữa
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Nếu không có địa chỉ mặc định thì fallback sang thông tin người dùng
        if (currentUser != null) {
            txtCustomerName.setText(currentUser.getName());
            txtCustomerPhone.setText(currentUser.getPhone_number());
            txtCustomerAddress.setText(currentUser.getAddress());
        }
    }


    private String loadDefaultAddress() {
        SharedPreferences prefs = getSharedPreferences("AddressPrefs", MODE_PRIVATE);
        String json = prefs.getString("default_address_" + userId, "");
        if (!json.isEmpty()) {
            try {
                Address address = new Gson().fromJson(json, Address.class);
                return address.getAddress();
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void displayProductInfo() {
        txtProductName.setText(selectedProduct.getNameproduct());
        txtProductQuantity.setText("Số lượng: " + quantity);
        txtProductColor.setText(selectedColor);
        txtProductSize.setText("Size: " + (selectedSize.isEmpty() ? "Không có" : selectedSize));
        productPrice = selectedProduct.getPrice();
        txtProductPrice.setText(formatPrice(productPrice));
        Glide.with(this)
                .load(ApiClient.IMAGE_URL + selectedProduct.getAvt_imgproduct())
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgProduct);
        updateTotalPriceDisplay();
    }

    private String formatPrice(int price) {
        return NumberFormat.getNumberInstance(Locale.US).format(price) + "₫";
    }

    private void updateTotalPriceDisplay() {
        String jsonCart = getIntent().getStringExtra("cart_list");
        int totalProductPrice = 0;

        if (jsonCart != null && !jsonCart.isEmpty()) {
            // Đặt hàng từ giỏ hàng
            List<Cart> cartList = new Gson().fromJson(jsonCart, new com.google.gson.reflect.TypeToken<List<Cart>>() {}.getType());
            for (Cart cart : cartList) {
                totalProductPrice += cart.getIdProduct().getPrice() * cart.getQuantity();
            }
        } else if (selectedProduct != null) {
            // Đặt hàng một sản phẩm
            totalProductPrice = productPrice * quantity;
        }

        txtProductTotal.setText(formatPrice(totalProductPrice));
        txtShippingFee.setText(formatPrice(shippingFee));
        txtTotalPayment.setText(formatPrice(totalProductPrice + shippingFee));
    }


    private void updateShippingFeeBasedOnAddress() {
        String address = txtCustomerAddress.getText().toString();
        Log.d("ShippingDebug", "Địa chỉ hiện tại: " + address); // ✅ Debug log

        boolean isFast = radioFastShipping.isChecked();
        shippingFee = calculateShippingFeeByAddress(address, isFast);

        if (txtShippingNote != null) {
            txtShippingNote.setText(
                    address.toLowerCase().contains("hà nội") || address.toLowerCase().contains("hcm")
                            ? "Áp dụng phí nội thành"
                            : "Áp dụng phí ngoại thành");
        }

        updateTotalPriceDisplay();
    }

    private int calculateShippingFeeByAddress(String address, boolean isFastShipping) {
        if (address == null || address.isEmpty()) {
            return isFastShipping ? 50000 : 30000; // mặc định nếu không rõ địa chỉ
        }

        address = address.toLowerCase();

        // Danh sách địa chỉ gần (nội thành hoặc tỉnh gần)
        boolean isNear = address.contains("hà nội")
                || address.contains("ha noi")
                || address.contains("tp.hcm")
                || address.contains("hồ chí minh")
                || address.contains("ho chi minh")
                || address.contains("tphcm")
                || address.contains("thành phố hồ chí minh")
                || address.contains("hải phòng")
                || address.contains("đà nẵng")
                || address.contains("bình dương")
                || address.contains("đồng nai");

        // Danh sách địa chỉ xa (tỉnh miền núi, vùng sâu vùng xa)
        boolean isFar = address.contains("sơn la")
                || address.contains("điện biên")
                || address.contains("cao bằng")
                || address.contains("hà giang")
                || address.contains("lào cai")
                || address.contains("kon tum")
                || address.contains("gia lai")
                || address.contains("phú yên");

        // Tính phí dựa trên khoảng cách và loại giao hàng
        if (isNear) {
            return isFastShipping ? 40000 : 20000;
        } else if (isFar) {
            return isFastShipping ? 60000 : 40000;
        } else {
            return isFastShipping ? 50000 : 30000; // trung bình
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ADDRESS && resultCode == RESULT_OK && data != null) {
            String json = data.getStringExtra("address_result");
            if (json != null) {
                Address selectedAddress = new Gson().fromJson(json, Address.class);
                if (selectedAddress != null) {
                    txtCustomerName.setText(selectedAddress.getName());
                    txtCustomerPhone.setText(selectedAddress.getPhone());
                    txtCustomerAddress.setText(selectedAddress.getAddress());

                    // Cập nhật lại địa chỉ mặc định vào SharedPreferences
                    SharedPreferences.Editor editor = getSharedPreferences("AddressPrefs", MODE_PRIVATE).edit();
                    editor.putString("default_address_" + userId, json);
                    editor.apply();

                    // Gán lại vào currentUser nếu cần đồng bộ
                    if (currentUser != null) {
                        currentUser.setName(selectedAddress.getName());
                        currentUser.setPhone_number(selectedAddress.getPhone());
                        currentUser.setAddress(selectedAddress.getAddress());
                    }

                    updateShippingFeeBasedOnAddress();
                }
            }
        }
    }


    private void calculateTotalFromCart(List<Cart> cartList) {
        int totalProductPrice = 0;
        for (Cart cart : cartList) {
            totalProductPrice += cart.getIdProduct().getPrice() * cart.getQuantity();
        }
        txtProductTotal.setText(formatPrice(totalProductPrice));
        txtShippingFee.setText(formatPrice(shippingFee));
        txtTotalPayment.setText(formatPrice(totalProductPrice + shippingFee));
    }
}
