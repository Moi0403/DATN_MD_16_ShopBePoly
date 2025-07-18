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
import java.util.PrimitiveIterator;

import okhttp3.ResponseBody;
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
    private List<String> selectedCartIds = new ArrayList<>();


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

//            startActivity(new Intent(ThanhToan.this, Dathangthanhcong.class));
//            finish();
            createNewOrder(name, phone, address, selectedPaymentId, selectedBankId);
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
        // Ưu tiên lấy địa chỉ mặc định nếu có
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
        // Nếu không có địa chỉ mặc định, thử lấy địa chỉ đầu tiên trong danh sách địa chỉ (nếu có)
        String allAddressesJson = prefs.getString("address_list_" + userId, "[]");
        List<Address> addressList = new Gson().fromJson(allAddressesJson, new com.google.gson.reflect.TypeToken<List<Address>>() {}.getType());
        if (addressList != null && !addressList.isEmpty()) {
            Address firstAddress = addressList.get(0);
            txtCustomerName.setText(firstAddress.getName());
            txtCustomerPhone.setText(firstAddress.getPhone());
            txtCustomerAddress.setText(firstAddress.getAddress());
            return;
        }
        // Nếu không có địa chỉ nào, để trống toàn bộ các trường
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
                // Lấy địa chỉ vừa chọn và hiển thị lên giao diện
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
            // Nếu không có address_result thì fallback về mặc định
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
            Order newOrder = new Order();

            // Ensure id_user is set as a User object with only the ID,
            // so UserTypeAdapter can handle it when serializing.
            User user = new User();
            user.setId(userId); // Assuming 'userId' is available in ThanhToan activity
            newOrder.setId_user(user);

            newOrder.setDate(getCurrentDate());
            newOrder.setStatus("Đang xử lý");
            newOrder.setAddress(address);
            newOrder.setPay(getPaymentMethodText(paymentId, bankId));

            String jsonCart = getIntent().getStringExtra("cart_list");
            int totalAmount = 0;
            // ✅ Direct list of ProductInOrder
            List<ProductInOrder> productsInOrderList = new ArrayList<>();

            if (jsonCart != null && !jsonCart.isEmpty()) {
                // Use the Gson instance that knows how to parse Cart objects (if Cart contains complex types)
                // or a simple new Gson() if Cart is straightforward.
                List<Cart> cartList = new Gson().fromJson(jsonCart, new com.google.gson.reflect.TypeToken<List<Cart>>() {}.getType());

                for (Cart cart : cartList) {
                    ProductInOrder productInOrder = new ProductInOrder();

                    // ✅ Create a Product object and set its ID.
                    // The ProductIdTypeAdapter's 'write' method will then take this Product object
                    // and serialize only its ID as a String for the API request.
                    Product productForOrder = new Product();
                    productForOrder.set_id(cart.getIdProduct().get_id()); // Use getId() from your Product DTO

                    productInOrder.setId_product(productForOrder); // Set the Product object

                    productInOrder.setQuantity(cart.getQuantity());
                    productInOrder.setPrice(cart.getIdProduct().getPrice());
                    productInOrder.setColor(cart.getColor());
                    productInOrder.setSize(cart.getSize()+"");
                    productInOrder.setImg(cart.getIdProduct().getAvt_imgproduct()); // Use getAvtImgproduct()

                    totalAmount += cart.getIdProduct().getPrice() * cart.getQuantity();
                    productsInOrderList.add(productInOrder);
                    selectedCartIds.add(cart.get_id());
                }
            } else if (selectedProduct != null) {
                ProductInOrder productInOrder = new ProductInOrder();

                // ✅ Create a Product object and set its ID for selectedProduct case
                Product productForOrder = new Product();
                productForOrder.set_id(selectedProduct.get_id()); // Use getId() from your Product DTO

                productInOrder.setId_product(productForOrder); // Set the Product object

                productInOrder.setQuantity(quantity);
                productInOrder.setPrice(selectedProduct.getPrice());
                productInOrder.setColor(selectedColor);
                productInOrder.setSize(selectedSize);
                productInOrder.setImg(selectedProduct.getAvt_imgproduct()); // Use getAvtImgproduct()

                totalAmount += selectedProduct.getPrice() * quantity;
                productsInOrderList.add(productInOrder);
            }

            totalAmount += shippingFee;
            newOrder.setTotal(String.valueOf(totalAmount));
            newOrder.setProducts(productsInOrderList); // ✅ Set the list of ProductInOrder objects

            // Calculate and set quantity_order
            int totalQuantity = 0;
            for (ProductInOrder pio : productsInOrderList) {
                totalQuantity += pio.getQuantity();
            }
            newOrder.setQuantity_order(totalQuantity);

            createOrderViaAPI(newOrder); // Call your API method

        } catch (Exception e) {
            Log.e(TAG, "Error creating order", e);
            Toast.makeText(this, "Lỗi tạo đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }




    private String getCurrentDate() {
        // ISO 8601: yyyy-MM-dd'T'HH:mm:ss'Z' (hoặc không cần 'Z')
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new java.util.Date());
    }


    private String getPaymentMethodText(int paymentId, int bankId){
        try {
            if (paymentId == R.id.radioAppBank){
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
        //Hiển thị loading
        btnDatHang.setEnabled(false);
        btnDatHang.setText("Đang xử lý ...");

        ApiService apiService = ApiClient.getApiService();

        Call<Order> call = apiService.createOrder(order);

        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                //reset button
                btnDatHang.setEnabled(true);
                btnDatHang.setText("Đặt hàng");

                if (response.isSuccessful() && response.body() != null){
                    Toast.makeText(ThanhToan.this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();

                    //xóa cart nếu đặt hàng từ cart
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
                    //nếu Api fail, lưu vào local
                    Log.e(TAG, "API create order failed: " + response.code());
                    saveOrderToLocal(order);
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                //reset button
                btnDatHang.setEnabled(true);
                btnDatHang.setText("Đặt hàng");

                Log.e(TAG, "API create order failed", t);
                //lưu vào local khi API fail
                saveOrderToLocal(order);
            }
        });

    }

    private void saveOrderToLocal(Order order){
        try {
            SharedPreferences prefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            //tạo ID cho order
            String orderId = "ORD" + System.currentTimeMillis();
            order.set_id(orderId);

            //láy danh sách order hiện có
            String existingOrders = prefs.getString("orders_list", "[]");
            List<Order> orderList = new Gson().fromJson(existingOrders, new com.google.gson.reflect.TypeToken<List<Order>>() {}.getType());

            if (orderList == null){
                orderList = new ArrayList<>();
            }

            //thêm order mới vào đầu danh sách
            orderList.add(0, order);

            //lưu lại
            String updatedOrders = new Gson().toJson(orderList);
            editor.putString("orders_list", updatedOrders);
            editor.apply();

            Toast.makeText(this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();

            //xóa cart nếu đặt hàng từ cart
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