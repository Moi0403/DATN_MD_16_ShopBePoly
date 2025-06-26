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
    private ImageView imgProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thanh_toan);

        initViews();
        getDataFromIntent();
        loadUserInfo();
        displayProductInfo();
        displayUserInfo();
        setupListeners();

//        // Ánh xạ view
//        ImageButton btnBack = findViewById(R.id.btnBack);
//        imgProduct = findViewById(R.id.imgProduct);
//        txtProductName = findViewById(R.id.txtProductName);
//        txtProductQuantity = findViewById(R.id.txtProductQuantity);
//        View viewProductColor = findViewById(R.id.viewProductColor);
//        txtProductSize = findViewById(R.id.txtProductSize);
//        txtProductPrice = findViewById(R.id.txtProductPrice);
//        txtProductTotal = findViewById(R.id.txtProductTotal);
//        TextView txtShippingFee = findViewById(R.id.txtShippingFee);
//        TextView txtTotalPayment = findViewById(R.id.txtTotalPayment);
//        // EditText cho thông tin khách hàng
//        EditText edtCustomerName = findViewById(R.id.edtCustomerName);
//        EditText edtCustomerEmail = findViewById(R.id.edtCustomerEmail);
//        EditText edtCustomerAddress = findViewById(R.id.edtCustomerAddress);
//        EditText edtCustomerPhone = findViewById(R.id.edtCustomerPhone);
//        // ...
//        RadioGroup radioGroupPaymentMain = findViewById(R.id.radioGroupPaymentMain);
//        RadioButton radioCOD = findViewById(R.id.radioCOD);
//        RadioButton radioAppBank = findViewById(R.id.radioAppBank);
//        View layoutBankOptions = findViewById(R.id.layoutBankOptions);
//        RadioGroup radioGroupBank = findViewById(R.id.radioGroupBank);
//        Button btnDatHang = findViewById(R.id.btnDatHang);

//        getDataFromIntent();
//
//        // Ẩn/hiện lựa chọn ngân hàng
//        radioGroupPaymentMain.setOnCheckedChangeListener((group, checkedId) -> {
//            if (checkedId == R.id.radioAppBank) {
//                layoutBankOptions.setVisibility(View.VISIBLE);
//            } else {
//                layoutBankOptions.setVisibility(View.GONE);
//                radioGroupBank.clearCheck();
//            }
//        });
//
//        // Nút back
//        btnBack.setOnClickListener(v -> finish());
//
//        // Xử lý nút Đặt hàng (ví dụ lấy phương thức thanh toán)
//        btnDatHang.setOnClickListener(v -> {
//            String paymentMethod = "";
//            if (radioCOD.isChecked()) {
//                paymentMethod = "Thanh toán khi nhận hàng";
//            } else if (radioAppBank.isChecked()) {
//                int checkedBankId = radioGroupBank.getCheckedRadioButtonId();
//                if (checkedBankId == R.id.radioMomo) {
//                    paymentMethod = "Momo";
//                } else if (checkedBankId == R.id.radioAgribank) {
//                    paymentMethod = "Agribank";
//                } else {
//                    paymentMethod = "Chưa chọn ngân hàng";
//                }
//            }
//            // Chuyển sang màn hình đặt hàng thành công
//            startActivity(new android.content.Intent(this, Dathangthanhcong.class));
//            // Nếu muốn đóng luôn màn thanh toán sau khi chuyển, bỏ comment dòng dưới:
//            // finish();
//        });
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
        txtCustomerEmail = findViewById(R.id.txtCustomerEmail);
        txtCustomerAddress = findViewById(R.id.txtCustomerAddress);
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
            txtCustomerName.setText(currentUser.getName());
            txtCustomerEmail.setText(currentUser.getEmail());
            txtCustomerPhone.setText(currentUser.getPhone_number());
            txtCustomerAddress.setText(currentUser.getAddress());
        }
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
}