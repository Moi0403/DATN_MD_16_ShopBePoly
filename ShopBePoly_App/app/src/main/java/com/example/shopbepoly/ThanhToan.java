package com.example.shopbepoly;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.API.CreateOrder;
import com.example.shopbepoly.DTO.Address;
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.ProductInOrder;
import com.example.shopbepoly.DTO.User;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class ThanhToan extends AppCompatActivity {

    private static final String TAG = "ThanhToan";

    private Product selectedProduct;
    private User currentUser;
    private int quantity = 1;
    private int productPrice = 0;
    private int shippingFee = 20000; // Mặc định phí giao hàng tiêu chuẩn Hà Nội
    private String selectedSize = "", selectedColor = "", userId;
    private List<String> selectedCartIds = new ArrayList<>();

    // ZaloPay
    private String pendingOrderId;
    private Order pendingOrder;

    private TextView txtProductName, txtProductColor, txtProductQuantity, txtProductSize, txtProductPrice,
            txtProductTotal, txtShippingFee, txtTotalPayment, txtCustomerName, txtCustomerAddress, txtCustomerPhone, txtShippingNote;
    private ImageView imgProduct, img_next_address;
    private RadioGroup radioGroupShipping, radioGroupPaymentMain;
    private RadioButton radioStandardShipping, radioFastShipping, radioCOD, radioZaloPay, radioAppBank;
    private LinearLayout layoutZaloPayInfo;
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

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2553, Environment.SANDBOX);

        radioStandardShipping.setChecked(true);

        shippingFee = 20000;

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

            int selectedPaymentId = radioGroupPaymentMain.getCheckedRadioButtonId();
            int selectedBankId = ((RadioGroup) findViewById(R.id.radioGroupBank)).getCheckedRadioButtonId();

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin khách hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra địa chỉ có thuộc Hà Nội không
            if (!isHanoiAddress(address)) {
                Toast.makeText(this, "Hiện tại chỉ giao hàng trong nội thành Hà Nội", Toast.LENGTH_LONG).show();
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

            // Check if ZaloPay is selected
            if (selectedPaymentId == R.id.radioZaloPay) {
                // Process ZaloPay payment
                processZaloPayPayment(name, phone, address, selectedPaymentId, selectedBankId);
            } else {
                // Process normal order
                createNewOrder(name, phone, address, selectedPaymentId, selectedBankId);
            }
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
        txtShippingNote = findViewById(R.id.txtShippingNote);
        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtCustomerAddress = findViewById(R.id.txtCustomerAddress);
        txtCustomerPhone = findViewById(R.id.txtCustomerPhone);
        img_next_address = findViewById(R.id.img_next_Adress);
        radioGroupShipping = findViewById(R.id.radioGroupShipping);
        radioStandardShipping = findViewById(R.id.radioStandardShipping);
        radioFastShipping = findViewById(R.id.radioFastShipping);
        radioGroupPaymentMain = findViewById(R.id.radioGroupPaymentMain);
        radioCOD = findViewById(R.id.radioCOD);
        radioZaloPay = findViewById(R.id.radioZaloPay);
        radioAppBank = findViewById(R.id.radioAppBank);
        layoutZaloPayInfo = findViewById(R.id.layoutZaloPayInfo);
        btnDatHang = findViewById(R.id.btnDatHang);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        radioGroupShipping.setOnCheckedChangeListener((group, checkedId) -> updateShippingFeeBasedOnAddress());

        radioGroupPaymentMain.setOnCheckedChangeListener((group, checkedId) -> {
            View layoutBankOptions = findViewById(R.id.layoutBankOptions);

            // Hide all optional layouts first
            layoutBankOptions.setVisibility(View.GONE);
            layoutZaloPayInfo.setVisibility(View.GONE);
            ((RadioGroup) findViewById(R.id.radioGroupBank)).clearCheck();

            if (checkedId == R.id.radioAppBank) {
                layoutBankOptions.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioZaloPay) {
                layoutZaloPayInfo.setVisibility(View.VISIBLE);
            }
        });

        img_next_address.setOnClickListener(v -> startActivityForResult(new Intent(this, AddressListActivity.class), REQ_ADDRESS));
    }

    /**
     * Process ZaloPay payment
     */
    private void processZaloPayPayment(String name, String phone, String address, int paymentId, int bankId) {
        try {
            // Create order first to get order ID
            pendingOrder = createOrderObject(name, phone, address, paymentId, bankId);
            pendingOrderId = pendingOrder.getIdOrder();

            // Calculate total amount
            int totalAmount = Integer.parseInt(pendingOrder.getTotal());

            // Create ZaloPay order
            createZaloPayOrder(totalAmount);

        } catch (Exception e) {
            Log.e(TAG, "Error processing ZaloPay payment", e);
            Toast.makeText(this, "Lỗi khởi tạo thanh toán ZaloPay: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create ZaloPay order and initiate payment
     */
    private void createZaloPayOrder(int amount) {
        try {
            CreateOrder orderApi = new CreateOrder();
            JSONObject data = orderApi.createOrder(String.valueOf(amount));

            if (data != null) {
                String code = data.getString("return_code");

                if (code.equals("1")) {
                    // Success - get token to pay
                    String token = data.getString("zp_trans_token");

                    // Show progress
                    btnDatHang.setEnabled(false);
                    btnDatHang.setText("Đang xử lý ZaloPay...");

                    // Call ZaloPay SDK to pay
                    ZaloPaySDK.getInstance().payOrder(ThanhToan.this, token, "demozpdk://app", new PayOrderListener() {
                        @Override
                        public void onPaymentSucceeded(String s, String s1, String s2) {
                            Log.d(TAG, "ZaloPay payment succeeded: " + s);

                            runOnUiThread(() -> {
                                // Update payment status
                                if (pendingOrder != null) {
                                    pendingOrder.setPay("ZaloPay - Đã thanh toán");
                                    createOrderViaAPI(pendingOrder);
                                }
                            });
                        }

                        @Override
                        public void onPaymentCanceled(String s, String s1) {
                            Log.d(TAG, "ZaloPay payment cancelled: " + s);

                            runOnUiThread(() -> {
                                btnDatHang.setEnabled(true);
                                btnDatHang.setText("Đặt hàng");
                                Toast.makeText(ThanhToan.this, "Đã hủy thanh toán ZaloPay", Toast.LENGTH_SHORT).show();

                                // Reset pending order
                                pendingOrder = null;
                                pendingOrderId = null;
                            });
                        }

                        @Override
                        public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                            Log.e(TAG, "ZaloPay payment error: " + zaloPayError.toString() + " - " + s);

                            runOnUiThread(() -> {
                                btnDatHang.setEnabled(true);
                                btnDatHang.setText("Đặt hàng");
                                Toast.makeText(ThanhToan.this, "Lỗi thanh toán ZaloPay: " + zaloPayError.toString(), Toast.LENGTH_LONG).show();

                                // Reset pending order
                                pendingOrder = null;
                                pendingOrderId = null;
                            });
                        }
                    });

                } else {
                    // Error from ZaloPay API
                    String message = data.has("return_message") ? data.getString("return_message") : "Lỗi không xác định";
                    Toast.makeText(this, "Lỗi tạo đơn hàng ZaloPay: " + message, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "ZaloPay API Error: " + data.toString());
                }
            } else {
                Toast.makeText(this, "Không thể kết nối đến ZaloPay", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error", e);
            Toast.makeText(this, "Lỗi xử lý dữ liệu ZaloPay", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "ZaloPay order creation error", e);
            Toast.makeText(this, "Lỗi tạo đơn hàng ZaloPay: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

    /**
     * Create order object without calling API
     */
    private Order createOrderObject(String name, String phone, String address, int paymentId, int bankId) {
        Order newOrder = new Order();

        // Generate random order ID
        String id_order = generateRandomOrderId();
        newOrder.setIdOrder(id_order);

        // Set user
        User user = new User();
        user.setId(userId);
        newOrder.setId_user(user);

        newOrder.setDate(getCurrentDate());
        newOrder.setStatus("Đang xử lý");
        newOrder.setAddress(address);
        newOrder.setPay(getPaymentMethodText(paymentId, bankId));

        String jsonCart = getIntent().getStringExtra("cart_list");
        int totalAmount = 0;
        List<ProductInOrder> productsInOrderList = new ArrayList<>();

        if (jsonCart != null && !jsonCart.isEmpty()) {
            List<Cart> cartList = new Gson().fromJson(
                    jsonCart, new com.google.gson.reflect.TypeToken<List<Cart>>() {}.getType()
            );

            for (Cart cart : cartList) {
                // 1) Lấy finalPrice, nếu chưa có thì tính theo sale/gốc
                int finalPrice = cart.getFinalPrice() > 0
                        ? cart.getFinalPrice()
                        : (cart.getIdProduct().getPrice_sale() > 0
                        ? cart.getIdProduct().getPrice_sale()
                        : cart.getIdProduct().getPrice());

                // optional: cập nhật vào cart để lần sau có sẵn
                cart.setFinalPrice(finalPrice);

                ProductInOrder pio = new ProductInOrder();
                Product productForOrder = new Product();
                productForOrder.set_id(cart.getIdProduct().get_id());
                pio.setId_product(productForOrder);

                pio.setQuantity(cart.getQuantity());
                pio.setColor(cart.getColor());
                pio.setSize(cart.getSize() + "");
                pio.setImg(cart.getIdProduct().getAvt_imgproduct());

                // 2) Nhét đúng giá hiển thị
                pio.setPrice(finalPrice);
                pio.setFinalPrice(finalPrice); // field bạn mới thêm

                // 3) Cộng tổng bằng finalPrice
                totalAmount += finalPrice * cart.getQuantity();

                productsInOrderList.add(pio);
                selectedCartIds.add(cart.get_id());
            }
        } else if (selectedProduct != null) {
            // Trường hợp mua 1 sản phẩm trực tiếp
            int finalPrice = selectedProduct.getPrice_sale() > 0
                    ? selectedProduct.getPrice_sale()
                    : selectedProduct.getPrice();

            ProductInOrder pio = new ProductInOrder();
            Product productForOrder = new Product();
            productForOrder.set_id(selectedProduct.get_id());
            pio.setId_product(productForOrder);

            pio.setQuantity(quantity);
            pio.setColor(selectedColor);
            pio.setSize(selectedSize);
            pio.setImg(selectedProduct.getAvt_imgproduct());

            pio.setPrice(finalPrice);
            pio.setFinalPrice(finalPrice);

            totalAmount += finalPrice * quantity;
            productsInOrderList.add(pio);
        }

        totalAmount += shippingFee;
        newOrder.setTotal(String.valueOf(totalAmount));
        newOrder.setProducts(productsInOrderList);

        // Calculate total quantity
        int totalQuantity = 0;
        for (ProductInOrder pio : productsInOrderList) {
            totalQuantity += pio.getQuantity();
        }
        newOrder.setQuantity_order(totalQuantity);

        return newOrder;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        ApiService apiService = ApiClient.getApiService();
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (User user : response.body()) {
                        if (user.getId().equals(userId)) {
                            currentUser = user;
                            displayUserInfo();
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
                    // Kiểm tra địa chỉ sau khi load
                    updateShippingFeeBasedOnAddress();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String allAddressesJson = prefs.getString("address_list_" + userId, "[]");
        List<Address> addressList = new Gson().fromJson(allAddressesJson, new com.google.gson.reflect.TypeToken<List<Address>>() {}.getType());
        if (addressList != null && !addressList.isEmpty()) {
            Address firstAddress = addressList.get(0);
            txtCustomerName.setText(firstAddress.getName());
            txtCustomerPhone.setText(firstAddress.getPhone());
            txtCustomerAddress.setText(firstAddress.getAddress());
            // Kiểm tra địa chỉ sau khi load
            updateShippingFeeBasedOnAddress();
            return;
        }

        txtCustomerName.setText("");
        txtCustomerPhone.setText("");
        txtCustomerAddress.setText("");
        txtCustomerName.setHint("Tên khách hàng");
        txtCustomerPhone.setHint("Số điện thoại");
        txtCustomerAddress.setHint("Địa chỉ");
    }

    private com.example.shopbepoly.DTO.Address loadDefaultAddressObject() {
        SharedPreferences prefs = getSharedPreferences("AddressPrefs", MODE_PRIVATE);
        String addressJson = prefs.getString("default_address_" + userId, "");
        if (!addressJson.isEmpty()) {
            try {
                return new Gson().fromJson(addressJson, com.example.shopbepoly.DTO.Address.class);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing default address object", e);
            }
        }
        return null;
    }

    private void displayProductInfo() {
        txtProductName.setText(selectedProduct.getNameproduct());
        txtProductQuantity.setText("Số lượng: " + quantity);
        txtProductColor.setText(selectedColor);
        txtProductSize.setText("Size: " + (selectedSize.isEmpty() ? "Không có" : selectedSize));
        if (selectedProduct.getPrice_sale() > 0) {
            productPrice = selectedProduct.getPrice_sale();
        } else {
            productPrice = selectedProduct.getPrice();
        }
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
            List<Cart> cartList = new Gson().fromJson(jsonCart, new com.google.gson.reflect.TypeToken<List<Cart>>() {}.getType());
            for (Cart cart : cartList) {
                totalProductPrice += cart.getIdProduct().getPrice() * cart.getQuantity();
            }
        } else if (selectedProduct != null) {
            totalProductPrice = productPrice * quantity;
        }

        txtProductTotal.setText(formatPrice(totalProductPrice));
        txtShippingFee.setText(formatPrice(shippingFee));
        txtTotalPayment.setText(formatPrice(totalProductPrice + shippingFee));
    }
    private boolean isHanoiInnerCity(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }

        address = address.toLowerCase().trim();

        // Các quận nội thành Hà Nội
        String[] innerDistricts = {
                "ba đình", "hoàn kiếm", "hai bà trưng", "đống đa",
                "tây hồ", "cầu giấy", "thanh xuân", "hoàng mai",
                "long biên", "bắc từ liêm", "nam từ liêm", "hà đông"
        };

        for (String district : innerDistricts) {
            if (address.contains(district)) {
                return true;
            }
        }

        return false;
    }
    // Phương thức kiểm tra địa chỉ có thuộc Hà Nội không
    private boolean isHanoiAddress(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }

        address = address.toLowerCase().trim();

        // Kiểm tra từ khóa Hà Nội
        boolean hasHanoiKeyword = address.contains("hà nội") ||
                address.contains("ha noi") ||
                address.contains("hanoi") ||
                address.contains("hn");

        // Kiểm tra có thuộc nội thành hoặc ngoại thành không
        boolean isInnerOrOuter = isHanoiInnerCity(address) || isHanoiOuterCity(address);

        return hasHanoiKeyword || isInnerOrOuter;
    }

    // Cập nhật phương thức tính phí vận chuyển theo khu vực Hà Nội
    private int calculateShippingFeeByAddress(String address, boolean isFastShipping) {
        if (address == null || address.isEmpty()) {
            // Mặc định là phí giao hàng nội thành
            return isFastShipping ? 30000 : 20000;
        }

        // Kiểm tra nội thành Hà Nội
        if (isHanoiInnerCity(address)) {
            // Phí giao hàng nội thành Hà Nội
            return isFastShipping ? 30000 : 20000; // Giao nhanh: 30k, Tiêu chuẩn: 20k
        }
        // Kiểm tra ngoại thành Hà Nội
        else if (isHanoiOuterCity(address)) {
            // Phí giao hàng ngoại thành Hà Nội (cao hơn nội thành)
            return isFastShipping ? 50000 : 35000; // Giao nhanh: 50k, Tiêu chuẩn: 35k
        }
        // Kiểm tra có từ khóa Hà Nội nhưng không xác định được quận/huyện
        else if (address.toLowerCase().contains("hà nội") ||
                address.toLowerCase().contains("ha noi") ||
                address.toLowerCase().contains("hanoi")) {
            // Áp dụng phí nội thành làm mặc định
            return isFastShipping ? 30000 : 20000;
        }
        // Không thuộc Hà Nội
        else {
            return 0; // Không giao hàng
        }
    }
    private boolean isHanoiOuterCity(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }

        address = address.toLowerCase().trim();

        // Các huyện ngoại thành Hà Nội
        String[] outerDistricts = {
                "sóc sơn", "đông anh", "gia lâm", "mê linh",
                "thanh trì", "thường tín", "hoài đức", "đan phượng",
                "mỹ đức", "ứng hòa", "thạch thất", "quốc oai",
                "chương mỹ", "thanh oai", "phú xuyên", "ba vì"
        };

        for (String district : outerDistricts) {
            if (address.contains(district)) {
                return true;
            }
        }

        return false;
    }
    // Cập nhật phương thức tính phí vận chuyển theo khu vực Hà Nội
    private void updateShippingFeeBasedOnAddress() {
        String address = txtCustomerAddress.getText().toString();
        boolean isFast = radioFastShipping.isChecked();

        // Kiểm tra xem địa chỉ có thuộc Hà Nội không
        if (!isHanoiAddress(address)) {
            // Hiển thị thông báo không giao hàng
            if (txtShippingNote != null) {
                txtShippingNote.setText("⚠️ Hiện tại chỉ giao hàng trong địa bàn Hà Nội");
                txtShippingNote.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            // Vô hiệu hóa nút đặt hàng
            btnDatHang.setEnabled(false);
            btnDatHang.setText("Không giao hàng đến khu vực này");

            // Set phí vận chuyển = 0
            shippingFee = 0;
            txtShippingFee.setText("0₫");

            // Tính lại tổng tiền
            updateTotalPriceDisplay();

            return;
        }

        // Nếu là địa chỉ Hà Nội hợp lệ
        shippingFee = calculateShippingFeeByAddress(address, isFast);

        // Kích hoạt lại nút đặt hàng
        btnDatHang.setEnabled(true);
        btnDatHang.setText("Đặt hàng");

        // Hiển thị thông báo phí giao hàng dựa trên khu vực
        if (txtShippingNote != null) {
            String noteText = "";
            int noteColor = android.R.color.holo_green_dark;

            if (isHanoiInnerCity(address)) {
                noteText = "✓ Giao hàng nội thành Hà Nội";
            } else if (isHanoiOuterCity(address)) {
                noteText = "✓ Giao hàng ngoại thành Hà Nội";
                noteColor = android.R.color.holo_orange_dark; // Màu cam cho ngoại thành
            } else {
                noteText = "✓ Giao hàng trong địa bàn Hà Nội";
            }

            txtShippingNote.setText(noteText);
            txtShippingNote.setTextColor(getResources().getColor(noteColor));
        }

        updateTotalPriceDisplay();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ADDRESS && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("address_result")) {
                String addressJson = data.getStringExtra("address_result");
                if (addressJson != null && !addressJson.isEmpty()) {
                    Address address = new Gson().fromJson(addressJson, Address.class);
                    txtCustomerName.setText(address.getName());
                    txtCustomerPhone.setText(address.getPhone());
                    txtCustomerAddress.setText(address.getAddress());
                    updateShippingFeeBasedOnAddress();
                    return;
                }
            }
            displayUserInfo();
            updateShippingFeeBasedOnAddress();
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

    private void createNewOrder(String name, String phone, String address, int paymentId, int bankId) {
        try {
            Order newOrder = createOrderObject(name, phone, address, paymentId, bankId);
            createOrderViaAPI(newOrder);
        } catch (Exception e) {
            Log.e(TAG, "Error creating order", e);
            Toast.makeText(this, "Lỗi tạo đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new java.util.Date());
    }

    private String generateRandomOrderId() {
        String datePart = new java.text.SimpleDateFormat("yyMMdd", Locale.getDefault()).format(new java.util.Date());
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomPart = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            randomPart.append(chars.charAt(random.nextInt(chars.length())));
        }

        return "" + datePart + randomPart.toString();
    }

    private String getPaymentMethodText(int paymentId, int bankId){
        try {
            if (paymentId == R.id.radioZaloPay) {
                return "ZaloPay - Ví điện tử";
            } else if (paymentId == R.id.radioAppBank){
                RadioButton selectedBank = findViewById(bankId);
                return "Chuyển khoản - " + (selectedBank != null ? selectedBank.getText().toString() : "Ngân Hàng");
            } else {
                RadioButton selectedPayment = findViewById(paymentId);
                return selectedPayment != null ? selectedPayment.getText().toString() : "Tiền mặt";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting payment method", e);
            return "Tiền mặt";
        }
    }

    private void createOrderViaAPI(Order order){
        btnDatHang.setEnabled(false);
        btnDatHang.setText("Đang xử lý ...");

        ApiService apiService = ApiClient.getApiService();
        Call<Order> call = apiService.createOrder(order);

        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                btnDatHang.setEnabled(true);
                btnDatHang.setText("Đặt hàng");

                if (response.isSuccessful() && response.body() != null){
                    Toast.makeText(ThanhToan.this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();
                    sendLocalNotification();

                    String jsonCart = getIntent().getStringExtra("cart_list");
                    if (!selectedCartIds.isEmpty()) {
                        clearSelectedCartAfterOrder(selectedCartIds);
                    }

                    startActivity(new Intent(ThanhToan.this, DonMua.class));
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("reload_cart", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Log.e(TAG, "API create order failed: " + response.code());
                    saveOrderToLocal(order);
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                btnDatHang.setEnabled(true);
                btnDatHang.setText("Đặt hàng");

                Log.e(TAG, "API create order failed", t);
                saveOrderToLocal(order);
            }
        });
    }

    private void sendLocalNotification() {
        String channelId = "order_channel_id";
        String channelName = "Đơn hàng";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setDescription("Thông báo đơn hàng mới");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle("Đặt hàng thành công")
                .setContentText("Cảm ơn bạn đã đặt hàng tại ShopBePoly!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void saveOrderToLocal(Order order){
        try {
            SharedPreferences prefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            String id_order = generateRandomOrderId();
            order.setIdOrder(id_order);

            String existingOrders = prefs.getString("orders_list", "[]");
            List<Order> orderList = new Gson().fromJson(existingOrders, new com.google.gson.reflect.TypeToken<List<Order>>() {}.getType());

            if (orderList == null){
                orderList = new ArrayList<>();
            }

            orderList.add(0, order);

            String updatedOrders = new Gson().toJson(orderList);
            editor.putString("orders_list", updatedOrders);
            editor.apply();

            Toast.makeText(this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();

            String jsonCart = getIntent().getStringExtra("cart_list");
            if (!selectedCartIds.isEmpty()) {
                clearSelectedCartAfterOrder(selectedCartIds);
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("delete_selected", true);
            setResult(RESULT_OK, resultIntent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Error saving order locally", e);
            Toast.makeText(this, "Lỗi lưu đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearSelectedCartAfterOrder(List<String> selectedCartIds){
        try {
            ApiService apiService = ApiClient.getApiService();
            Map<String, List<String>> body = new HashMap<>();
            body.put("cartIds", selectedCartIds);

            apiService.deleteCartItems(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()){
                        Log.d(TAG, "Selected cart items cleared successfully");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Failed to clear selected cart items", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error clearing selected cart items", e);
        }
    }
}