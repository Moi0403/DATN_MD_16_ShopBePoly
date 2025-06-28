package com.example.shopbepoly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.User;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private String selectedSize = "";
    private int productPrice = 0;
    private int shippingFee = 30000;

    private TextView txtProductName, txtProductQuantity, txtProductSize, txtProductPrice, txtProductTotal,txtShippingFee, txtTotalPayment,txtCustomerName, txtCustomerEmail, txtCustomerAddress, txtCustomerPhone;
    private ImageView imgProduct,img_next_address;

    private static final int REQ_ADDRESS = 3001;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thanh_toan);
        // Lấy userId
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        initViews();
        getDataFromIntent();
        loadUserInfo();
        displayProductInfo();
        loadDefaultAddressOnStartup();
        setupListeners();
    }

    private void initViews(){
        imgProduct = findViewById(R.id.imgProduct);
        txtProductName = findViewById(R.id.txtProductName);
        txtProductQuantity = findViewById(R.id.txtProductQuantity);
        View viewProductColor = findViewById(R.id.viewProductColor);
        txtProductSize = findViewById(R.id.txtProductSize);
        txtProductPrice = findViewById(R.id.txtProductPrice);
        txtProductTotal = findViewById(R.id.txtProductTotal);
        txtShippingFee = findViewById(R.id.txtShippingFee);
        txtTotalPayment = findViewById(R.id.txtTotalPayment);

        txtCustomerName = findViewById(R.id.txtCustomerName);
//        txtCustomerEmail = findViewById(R.id.txtCustomerEmail);
        txtCustomerAddress = findViewById(R.id.txtCustomerAddress);
        img_next_address = findViewById(R.id.img_next_Adress);
        txtCustomerPhone = findViewById(R.id.txtCustomerPhone);

    }

    private void setupListeners(){
        ImageView btnBack = findViewById(R.id.btnBack);
        RadioGroup radioGroupPaymentMain = findViewById(R.id.radioGroupPaymentMain);
        RadioButton radioCOD = findViewById(R.id.radioCOD);
        RadioButton radioAppBank = findViewById(R.id.radioAppBank);
        View layoutBankOptions = findViewById(R.id.layoutBankOptions);
        RadioGroup radioGroupBank = findViewById(R.id.radioGroupBank);
        Button btnDatHang = findViewById(R.id.btnDatHang);

        // Ẩn/hiện lựa chọn ngân hàng
        radioGroupPaymentMain.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioAppBank) {
                layoutBankOptions.setVisibility(View.VISIBLE);
            } else {
                layoutBankOptions.setVisibility(View.GONE);
                radioGroupBank.clearCheck();
            }
        });

        //nut back
        btnBack.setOnClickListener(v -> finish());

        img_next_address.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressListActivity.class);
            startActivityForResult(intent, REQ_ADDRESS);
        });
    }
    private void getDataFromIntent(){
        Intent intent = getIntent();
        //lay thong tin san pham
        if (intent.hasExtra("product")){
            String productJson = intent.getStringExtra("product");
            selectedProduct = new Gson().fromJson(productJson, Product.class);

            // Lấy thông tin khác
            quantity = intent.getIntExtra("quantity", 1);
            selectedSize = intent.getStringExtra("size");
            if (selectedSize == null) selectedSize = "";

            Log.d(TAG, "Product: " + (selectedProduct != null ? selectedProduct.getNameproduct() : "null"));
            Log.d(TAG, "Quantity: " + quantity);
            Log.d(TAG, "Size: " + selectedSize);
        } else {
            Log.e(TAG, "No product data found in intent");
            Toast.makeText(this, "Khong co thong tin san pham", Toast.LENGTH_SHORT).show();
        }
    }
    private void loadUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        ApiService apiService = ApiClient.getApiService();
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (User user : response.body()) {
                        if (user.getId().equals(userId)) {
                            currentUser = user; // ✅ Gán user vào currentUser
                            displayUserInfo(); // ✅ Hiển thị sau khi có dữ liệu
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(ThanhToan.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void displayUserInfo(){
        if (currentUser != null){
            // Chỉ hiển thị thông tin từ user nếu chưa có thông tin từ địa chỉ mặc định
            if (txtCustomerName.getText().toString().isEmpty()) {
                txtCustomerName.setText(currentUser.getName());
            }
            // Email luôn hiển thị từ user, không bị ghi đè
//            txtCustomerEmail.setText(currentUser.getEmail());
            if (txtCustomerPhone.getText().toString().isEmpty()) {
                txtCustomerPhone.setText(currentUser.getPhone_number());
            }
            if (txtCustomerAddress.getText().toString().isEmpty()) {
                String defaultAddress = loadDefaultAddress();
                if (defaultAddress != null && !defaultAddress.isEmpty()) {
                    txtCustomerAddress.setText(defaultAddress);
                } else {
                    // Nếu không có địa chỉ mặc định, hiển thị địa chỉ từ user
                    txtCustomerAddress.setText(currentUser.getAddress());
                }
            }
        }
    }

    private String loadDefaultAddress() {
        SharedPreferences prefs = getSharedPreferences("AddressPrefs", MODE_PRIVATE);
        String addressJson = prefs.getString("default_address_" + userId, "");
        if (!addressJson.isEmpty()) {
            try {
                com.example.shopbepoly.DTO.Address defaultAddress = new Gson().fromJson(addressJson, com.example.shopbepoly.DTO.Address.class);
                if (defaultAddress != null) {
                    return defaultAddress.getAddress();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing default address", e);
            }
        }
        return null;
    }

    private com.example.shopbepoly.DTO.Address loadDefaultAddressObject() {
        SharedPreferences prefs = getSharedPreferences("AddressPrefs", MODE_PRIVATE);
        String addressJson = prefs.getString("default_address_" + userId, "");
        if (!addressJson.isEmpty()) {
            try {
                com.example.shopbepoly.DTO.Address defaultAddress = new Gson().fromJson(addressJson, com.example.shopbepoly.DTO.Address.class);
                if (defaultAddress != null) {
                    return defaultAddress;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing default address object", e);
            }
        }
        return null;
    }

    private void displayProductInfo() {
        if (selectedProduct == null) {
            Toast.makeText(this, "Không có thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị thông tin sản phẩm
        txtProductName.setText(selectedProduct.getNameproduct());
        txtProductQuantity.setText("Số lượng: " + quantity);
        txtProductSize.setText("Size: " + (selectedSize.isEmpty() ? "Không có" : selectedSize));

        productPrice = selectedProduct.getPrice();
        txtProductPrice.setText(formatPrice(productPrice));

        int totalProductPrice = productPrice * quantity;
        txtProductTotal.setText(formatPrice(totalProductPrice));

        // Load hình ảnh sản phẩm
        if (selectedProduct.getAvt_imgproduct() != null && !selectedProduct.getAvt_imgproduct().isEmpty()){
            String imageUrl = ApiClient.IMAGE_URL + selectedProduct.getAvt_imgproduct();
            Log.d(TAG, "Loading image from: " + imageUrl);

            Glide.with(this)
                            .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_background)
                                            .error(R.drawable.ic_launcher_background)
                                                    .into(imgProduct);

//            Picasso.get()
//                    .load(imageUrl)
//                    .placeholder(R.drawable.ic_launcher_background)
//                    .error(R.drawable.ic_launcher_background)
//                    .into(imgProduct);
        } else {
            Log.w(TAG, "No product image available");
            imgProduct.setImageResource(R.drawable.ic_launcher_background);
        }

        Log.d(TAG, "product info displayed successfully");
    }
    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(price) + "₫";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ADDRESS && resultCode == RESULT_OK && data != null) {
            String addressJson = data.getStringExtra("address_result");
            if (addressJson != null) {
                try {
                    com.example.shopbepoly.DTO.Address selectedAddress = new Gson().fromJson(addressJson, com.example.shopbepoly.DTO.Address.class);
                    if (selectedAddress != null) {
                        // Lưu địa chỉ mặc định vào SharedPreferences (bền vững)
                        SharedPreferences prefs = getSharedPreferences("AddressPrefs", MODE_PRIVATE);
                        prefs.edit()
                            .putString("default_address_" + userId, addressJson)
                            .putLong("last_updated", System.currentTimeMillis())
                            .apply();
                        // Cập nhật thông tin người nhận (không bao gồm email)
                        txtCustomerName.setText(selectedAddress.getName());
                        txtCustomerPhone.setText(selectedAddress.getPhone());
                        txtCustomerAddress.setText(selectedAddress.getAddress());
                        // Cập nhật thông tin user với địa chỉ mới
                        if (currentUser != null) {
                            currentUser.setAddress(selectedAddress.getAddress());
                            // Cập nhật lên server (nếu cần)
                            updateUserAddressOnServer(selectedAddress.getAddress());
                        }
                        Toast.makeText(this, "Đã chọn địa chỉ: " + selectedAddress.getAddress(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Default address saved: " + selectedAddress.getName() + " - " + selectedAddress.getPhone() + " - " + selectedAddress.getAddress());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing address data", e);
                    Toast.makeText(this, "Lỗi khi xử lý địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateUserAddressOnServer(String newAddress) {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        
        if (!userId.isEmpty()) {
            User updateUser = new User();
            updateUser.setAddress(newAddress);
            
            ApiService apiService = ApiClient.getApiService();
            apiService.updateUser(userId, updateUser).enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "User address updated successfully on server");
                    } else {
                        Log.e(TAG, "Failed to update user address on server");
                    }
                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {
                    Log.e(TAG, "Error updating user address on server", t);
                }
            });
        }
    }

    private void loadDefaultAddressOnStartup() {
        // Load địa chỉ mặc định ngay khi activity khởi tạo
        com.example.shopbepoly.DTO.Address defaultAddress = loadDefaultAddressObject();
        if (defaultAddress != null) {
            // Cập nhật thông tin người nhận (không bao gồm email)
            txtCustomerName.setText(defaultAddress.getName());
            txtCustomerPhone.setText(defaultAddress.getPhone());
            txtCustomerAddress.setText(defaultAddress.getAddress());
            
            Log.d(TAG, "Loaded default address info on startup: " + defaultAddress.getName() + " - " + defaultAddress.getPhone());
        }
    }
}