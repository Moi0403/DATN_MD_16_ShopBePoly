package com.example.shopbepoly;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
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
import com.example.shopbepoly.DTO.Address;
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.ProductInOrder;
import com.example.shopbepoly.DTO.User;
import com.google.gson.Gson;

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

public class ThanhToan extends AppCompatActivity implements VietQRPaymentHandler.PaymentCallback {

    private static final String TAG = "ThanhToan";

    private Product selectedProduct;
    private User currentUser;
    private int quantity = 1;
    private int productPrice = 0;
    private int shippingFee = 30000;
    private String selectedSize = "", selectedColor = "", userId;
    private List<String> selectedCartIds = new ArrayList<>();

    // VietQR Payment Handler
    private VietQRPaymentHandler vietQRHandler;
    private String pendingOrderId;
    private Order pendingOrder;

    private TextView txtProductName, txtProductColor, txtProductQuantity, txtProductSize, txtProductPrice,
            txtProductTotal, txtShippingFee, txtTotalPayment, txtCustomerName, txtCustomerAddress, txtCustomerPhone, txtShippingNote;
    private ImageView imgProduct, img_next_address;
    private RadioGroup radioGroupShipping, radioGroupPaymentMain;
    private RadioButton radioStandardShipping, radioFastShipping, radioCOD, radioVietQR, radioAppBank;
    private LinearLayout layoutVietQRInfo;
    private Button btnDatHang;

    private static final int REQ_ADDRESS = 3001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);

        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        // Initialize VietQR handler
        vietQRHandler = new VietQRPaymentHandler(this, this);

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

            int selectedPaymentId = radioGroupPaymentMain.getCheckedRadioButtonId();
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

            // Check if VietQR is selected
            if (selectedPaymentId == R.id.radioVietQR) {
                // Show VietQR payment dialog
                showVietQRPayment(name, phone, address, selectedPaymentId, selectedBankId);
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
        radioVietQR = findViewById(R.id.radioVietQR);
        radioAppBank = findViewById(R.id.radioAppBank);
        layoutVietQRInfo = findViewById(R.id.layoutVietQRInfo);
        btnDatHang = findViewById(R.id.btnDatHang);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        radioGroupShipping.setOnCheckedChangeListener((group, checkedId) -> updateShippingFeeBasedOnAddress());

        radioGroupPaymentMain.setOnCheckedChangeListener((group, checkedId) -> {
            View layoutBankOptions = findViewById(R.id.layoutBankOptions);

            // Hide all optional layouts first
            layoutBankOptions.setVisibility(View.GONE);
            layoutVietQRInfo.setVisibility(View.GONE);
            ((RadioGroup) findViewById(R.id.radioGroupBank)).clearCheck();

            if (checkedId == R.id.radioAppBank) {
                layoutBankOptions.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioVietQR) {
                layoutVietQRInfo.setVisibility(View.VISIBLE);
            }
        });

        img_next_address.setOnClickListener(v -> startActivityForResult(new Intent(this, AddressListActivity.class), REQ_ADDRESS));
    }

    /**
     * Show VietQR payment dialog
     */
    private void showVietQRPayment(String name, String phone, String address, int paymentId, int bankId) {
        try {
            // Create order first to get order ID
            pendingOrder = createOrderObject(name, phone, address, paymentId, bankId);
            pendingOrderId = pendingOrder.getIdOrder();

            // Calculate total amount
            int totalAmount = Integer.parseInt(pendingOrder.getTotal());

            // Show VietQR payment dialog
            if (vietQRHandler != null) {
                vietQRHandler.showPaymentDialog(pendingOrderId, totalAmount, name);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error showing VietQR payment", e);
            Toast.makeText(this, "Lỗi khởi tạo thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create order object without calling API
     */
    private Order createOrderObject(String name, String phone, String address, int paymentId, int bankId) {
        Order newOrder = new Order();

        String id_order = generateRandomOrderId();
        newOrder.setIdOrder(id_order);

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

        int totalQuantity = 0;
        for (ProductInOrder p : productsInOrderList) totalQuantity += p.getQuantity();
        newOrder.setQuantity_order(totalQuantity);

        return newOrder;
    }

    // VietQR Payment Callback Methods
    @Override
    public void onPaymentConfirmed(String orderId, int amount) {
        Log.d(TAG, "Payment confirmed for order: " + orderId);

        if (pendingOrder != null) {
            // Update payment status
            pendingOrder.setPay("VietQR - Đã thanh toán");

            // Create order via API
            createOrderViaAPI(pendingOrder);
        } else {
            Toast.makeText(this, "Lỗi xử lý đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentCancelled() {
        Log.d(TAG, "Payment cancelled by user");
        Toast.makeText(this, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();

        // Reset pending order
        pendingOrder = null;
        pendingOrderId = null;
    }

    @Override
    public void onPaymentTimeout() {
        Log.d(TAG, "Payment timeout");
        Toast.makeText(this, "Phiên thanh toán đã hết hạn", Toast.LENGTH_LONG).show();

        // Reset pending order
        pendingOrder = null;
        pendingOrderId = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vietQRHandler != null) {
            vietQRHandler.cleanup();
        }
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
                int price = cart.getIdProduct().getPrice_sale() > 0
                        ? cart.getIdProduct().getPrice_sale()
                        : cart.getIdProduct().getPrice();
                totalProductPrice += price * cart.getQuantity();
            }
        } else if (selectedProduct != null) {
            totalProductPrice = productPrice * quantity;
        }

        txtProductTotal.setText(formatPrice(totalProductPrice));
        txtShippingFee.setText(formatPrice(shippingFee));
        txtTotalPayment.setText(formatPrice(totalProductPrice + shippingFee));
    }

    private void updateShippingFeeBasedOnAddress() {
        String address = txtCustomerAddress.getText().toString();
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
            return isFastShipping ? 50000 : 30000;
        }

        address = address.toLowerCase();

        boolean isNear = address.contains("hà nội") || address.contains("ha noi") || address.contains("tp.hcm")
                || address.contains("hồ chí minh") || address.contains("ho chi minh") || address.contains("tphcm")
                || address.contains("thành phố hồ chí minh") || address.contains("hải phòng")
                || address.contains("đà nẵng") || address.contains("bình dương") || address.contains("đồng nai");

        boolean isFar = address.contains("sơn la") || address.contains("điện biên") || address.contains("cao bằng")
                || address.contains("hà giang") || address.contains("lào cai") || address.contains("kon tum")
                || address.contains("gia lai") || address.contains("phú yên");

        if (isNear) return isFastShipping ? 40000 : 20000;
        else if (isFar) return isFastShipping ? 60000 : 40000;
        else return isFastShipping ? 50000 : 30000;
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
            int price = cart.getIdProduct().getPrice_sale() > 0
                    ? cart.getIdProduct().getPrice_sale()
                    : cart.getIdProduct().getPrice();
            totalProductPrice += price * cart.getQuantity();
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
            if (paymentId == R.id.radioVietQR) {
                return "VietQR - Chuyển khoản";
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