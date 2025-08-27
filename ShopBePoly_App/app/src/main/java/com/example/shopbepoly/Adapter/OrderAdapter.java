package com.example.shopbepoly.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Chitietdonhang;
import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.ProductInOrder;
import com.example.shopbepoly.DTO.Variation;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.DanhGia;
import com.google.gson.Gson;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private static final String TAG = "OrderAdapter";
    private static final String LOGIN_PREFS = "LoginPrefs";
    private static final String USER_ID_KEY = "userId";

    private Context context;
    private List<Order> ordList;
    private String userId;
    private Runnable onOrderCancelled;
    private boolean isStaff;

    public OrderAdapter(Context context, List<Order> list, Runnable onOrderCancelled) {
        this.context = context;
        this.ordList = list;
        this.onOrderCancelled = onOrderCancelled;
        this.userId = getUserIdFromPreferences();
        this.isStaff = false;
    }

    public OrderAdapter(Context context, List<Order> list, Runnable onOrderCancelled, boolean isStaff) {
        this.context = context;
        this.ordList = list;
        this.onOrderCancelled = onOrderCancelled;
        this.userId = getUserIdFromPreferences();
        this.isStaff = isStaff;
    }

    private String getUserIdFromPreferences() {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE);
            return sharedPreferences.getString(USER_ID_KEY, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting userId from preferences", e);
            return null;
        }
    }

    public void setData(List<Order> list) {
        this.ordList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donhang, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = ordList.get(position);

        // Bind dữ liệu đơn hàng
        String orderCode = order.getIdOrder() != null ? order.getIdOrder() : order.get_id();
        holder.tvmaDH.setText("Mã đơn hàng: " + orderCode);

        String totalStr = order.getTotal();
        int amountToDisplay = 0;
        try {
            if (isZaloPayPaid(order.getPay())) {
                amountToDisplay = 0;
            } else if (totalStr != null && !totalStr.isEmpty()) {
                amountToDisplay = Integer.parseInt(totalStr);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing total: " + totalStr, e);
        }
        holder.tvthanhTien.setText(String.format("Thành tiền: %,d đ", amountToDisplay));
        holder.tvSoLuongSP.setText("Tổng số lượng sản phẩm: " + order.getQuantity_order());
        holder.tvTT.setText("Trạng thái: " + order.getStatus());

        // Hiển thị thời gian mua
        if (isStaff) {
            holder.tvngayMua.setText("Thời gian mua: " + formatDateForStaff(order.getDate()));
        } else {
            holder.tvngayMua.setText("Thời gian mua: " + formatDateForUser(order.getDate()));
        }

        // Hiển thị thời gian xác nhận - CHỈ KHI KHÔNG PHẢI TRẠNG THÁI "ĐANG XỬ LÝ"
        if (order.getStatus().equals("Đang xử lý")) {
            // Ẩn thời gian xác nhận khi đang chờ xác nhận
            holder.tvTimeUp.setVisibility(View.GONE);
        } else {
            // Hiển thị thời gian xác nhận cho các trạng thái khác
            holder.tvTimeUp.setVisibility(View.VISIBLE);
            if (isStaff) {
                holder.tvTimeUp.setText(formatDateForStaff(order.getCheckedAt()));
            } else {
                holder.tvTimeUp.setText(formatDateForUser(order.getCheckedAt()));
            }
        }

        // Hiển thị thông tin khách hàng nếu là staff
        if (isStaff) {
            holder.layoutCustomerInfo.setVisibility(View.VISIBLE);
            displayCustomerInfo(holder, order);
        } else {
            holder.layoutCustomerInfo.setVisibility(View.GONE);
        }

        // Hiển thị lý do hủy nếu đơn hàng bị hủy
        if ("Đã hủy".equalsIgnoreCase(order.getStatus())) {
            holder.tvLydo.setVisibility(View.VISIBLE);
            holder.tvLydo.setText("Lý do hủy: " + order.getCancelReason());
        } else {
            holder.tvLydo.setVisibility(View.GONE);
        }

        // Setup click listeners
        setupClickListeners(holder, order, position);

        // Cập nhật hiển thị nút theo trạng thái
        updateButtonVisibility(holder, order);
    }

    private void displayCustomerInfo(OrderViewHolder holder, Order order) {
        try {
            // Hiển thị tên khách hàng
            if (order.getId_user() != null && order.getId_user().getName() != null && !order.getId_user().getName().isEmpty()) {
                holder.tvCustomerName.setText("Tên KH: " + order.getId_user().getName());
                holder.tvCustomerName.setVisibility(View.VISIBLE);
            } else {
                holder.tvCustomerName.setVisibility(View.GONE);
            }

            // Hiển thị số điện thoại và xử lý sao chép
            if (order.getId_user() != null && order.getId_user().getPhone_number() != null && !order.getId_user().getPhone_number().isEmpty()) {
                String phoneNumber = order.getId_user().getPhone_number();
                holder.tvCustomerPhone.setText("SĐT: " + phoneNumber);
                holder.tvCustomerPhone.setVisibility(View.VISIBLE);
                holder.btnCopyPhone.setVisibility(View.VISIBLE);

                holder.btnCopyPhone.setOnClickListener(v -> copyToClipboard(phoneNumber, "Số điện thoại"));
            } else {
                holder.tvCustomerPhone.setVisibility(View.GONE);
                holder.btnCopyPhone.setVisibility(View.GONE);
            }

            // Hiển thị địa chỉ và xử lý sao chép
            if (order.getAddress() != null && !order.getAddress().isEmpty()) {
                String address = order.getAddress();
                holder.tvCustomerAddress.setText("Địa chỉ: " + address);
                holder.tvCustomerAddress.setVisibility(View.VISIBLE);
                holder.btnCopyAddress.setVisibility(View.VISIBLE);

                holder.btnCopyAddress.setOnClickListener(v -> copyToClipboard(address, "Địa chỉ"));
            } else {
                holder.tvCustomerAddress.setVisibility(View.GONE);
                holder.btnCopyAddress.setVisibility(View.GONE);
            }

            // Ẩn layout nếu không có thông tin nào
            if (holder.tvCustomerName.getVisibility() == View.GONE &&
                    holder.tvCustomerPhone.getVisibility() == View.GONE &&
                    holder.tvCustomerAddress.getVisibility() == View.GONE) {
                holder.layoutCustomerInfo.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying customer info", e);
            holder.layoutCustomerInfo.setVisibility(View.GONE);
        }
    }

    private void copyToClipboard(String text, String label) {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context, "Đã sao chép " + label + " vào clipboard", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error copying to clipboard", e);
            Toast.makeText(context, "Không thể sao chép " + label, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners(OrderViewHolder holder, Order order, int position) {
        // Nút Chi tiết
        holder.btnChiTiet.setOnClickListener(v -> {
            Intent intent = new Intent(context, Chitietdonhang.class);
            intent.putExtra("order", order);
            intent.putExtra("isStaff", isStaff);
            context.startActivity(intent);
        });

        // Nút Hủy đơn hàng
        holder.btnHuy.setOnClickListener(v -> showCancelDialog(order, position));

        // Nút Nhận hàng/Xác nhận
        holder.btnNhan.setOnClickListener(v -> showReceiveDialog(order));

        // Nút Mua lại
        holder.btnMuaLai.setOnClickListener(v -> showReorderDialog(order));
    }
    private String getStaffNameFromPreferences() {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE);
            return sharedPreferences.getString("name", null); // Lấy tên nhân viên đã đăng nhập
        } catch (Exception e) {
            Log.e(TAG, "Error getting staff name from preferences", e);
            return null;
        }
    }
    // Logic hiển thị nút được đơn giản hóa
    private void updateButtonVisibility(OrderViewHolder holder, Order order) {
        String status = order.getStatus().toLowerCase();
        String checkedBy = order.getCheckedBy();
        String delicercheckedBy = order.getDelicercheckedBy();

        // Ẩn tất cả các nút trước
        holder.btnHuy.setVisibility(View.GONE);
        holder.btnNhan.setVisibility(View.GONE);
        holder.btnMuaLai.setVisibility(View.GONE);

        switch (status) {
            case "đang xử lý":
                if (isStaff) {
                    holder.btnHuy.setVisibility(View.VISIBLE);
                    holder.btnNhan.setVisibility(View.VISIBLE);
                    holder.btnNhan.setText("Xác nhận đơn");
                    holder.btnNhan.setEnabled(true);
                } else {
                    holder.btnHuy.setVisibility(View.VISIBLE);
                }
                break;

            case "đang giao hàng":
                if (isStaff) {
                    // Kiểm tra xem nhân viên đã xác nhận đơn chưa
                    if (checkedBy != null && checkedBy.startsWith("staff_confirmed:")) {
                        // Kiểm tra xem nhân viên giao hàng đã xác nhận chưa
                        if (delicercheckedBy != null && delicercheckedBy.startsWith("delivery_confirmed:")) {
                            // Đã xác nhận giao hàng rồi, ẩn nút
                            holder.btnNhan.setVisibility(View.GONE);
                        } else {
                            // Chưa xác nhận giao hàng, hiển thị nút
                            holder.btnNhan.setVisibility(View.VISIBLE);
                            holder.btnNhan.setText("Xác nhận giao hàng");
                            holder.btnNhan.setEnabled(true);
                        }
                    } else {
                        // Chưa xác nhận đơn, có thể hiển thị nút xác nhận giao hàng trực tiếp
                        holder.btnNhan.setVisibility(View.VISIBLE);
                        holder.btnNhan.setText("Xác nhận giao hàng");
                        holder.btnNhan.setEnabled(true);
                    }
                } else {
                    // User chỉ hiển thị nút "Đã nhận hàng" khi nhân viên giao hàng đã xác nhận
                    if (delicercheckedBy != null && delicercheckedBy.startsWith("delivery_confirmed:")) {
                        holder.btnNhan.setVisibility(View.VISIBLE);
                        holder.btnNhan.setText("Đã nhận hàng");
                        holder.btnNhan.setEnabled(true);
                    } else {
                        holder.btnNhan.setVisibility(View.GONE);
                    }
                }
                break;

            case "đã hủy":
            case "đã giao hàng":
                holder.btnMuaLai.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showCancelDialog(Order order, int position) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_cancel_order, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        builder.setPositiveButton("Xác nhận hủy", (dialog, which) -> {
            RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupReasons);
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId != -1) {
                RadioButton selectedRadio = dialogView.findViewById(selectedId);
                String reason = selectedRadio.getText().toString();

                Order updateOrder = new Order();
                updateOrder.set_id(order.get_id());
                updateOrder.setStatus("Đã hủy");
                updateOrder.setCancelReason(reason);

                cancelOrder(updateOrder, position);
                Toast.makeText(context, "Lý do hủy: " + reason, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Vui lòng chọn lý do hủy", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy bỏ", null);
        builder.show();
    }

    // Logic xử lý nút nhận hàng được đơn giản hóa
    private void showReceiveDialog(Order order) {
        if (isStaff) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setIcon(R.drawable.thongbao);

            // Lấy tên nhân viên đã đăng nhập
            String staffName = getStaffNameFromPreferences();
            if (staffName == null || staffName.trim().isEmpty()) {
                Toast.makeText(context, "Không thể xác định thông tin nhân viên", Toast.LENGTH_SHORT).show();
                return;
            }

            // Nếu đang xử lý -> Xác nhận đơn
            if ("đang xử lý".equalsIgnoreCase(order.getStatus())) {
                builder.setTitle("Xác nhận đơn hàng");
                builder.setMessage("Xác nhận đã bàn giao cho đơn vị vận chuyển?");
                builder.setPositiveButton("Xác nhận", (dialog, which) -> {
                    Order orderUpdate = new Order();
                    orderUpdate.set_id(order.get_id());
                    orderUpdate.setStatus("Đang giao hàng");
                    orderUpdate.setCheckedAt(getCurrentTime());
                    orderUpdate.setCheckedBy("staff_confirmed:" + staffName); // Sử dụng checkedBy cho xác nhận đơn
                    updateOrder(orderUpdate);
                    Toast.makeText(context, "Đã xác nhận đơn hàng và chuyển sang trạng thái đang giao hàng", Toast.LENGTH_SHORT).show();
                });
            }
            // Nếu đang giao hàng -> Xác nhận giao hàng
            else if ("đang giao hàng".equalsIgnoreCase(order.getStatus())) {
                builder.setTitle("Xác nhận giao hàng");
                builder.setMessage("Xác nhận đã giao hàng thành công cho khách hàng?");
                builder.setPositiveButton("Xác nhận", (dialog, which) -> {
                    Order orderUpdate = new Order();
                    orderUpdate.set_id(order.get_id());
                    orderUpdate.setStatus("Đang giao hàng"); // Vẫn giữ trạng thái này
                    orderUpdate.setDelicercheckedAt(getCurrentTime()); // Sử dụng delicercheckedAt
                    orderUpdate.setDelicercheckedBy("" + staffName+ " - Nhân Viên"); // Sử dụng delicercheckedBy
                    Log.d("delivery_confirmed:" , staffName);
                    updateOrder(orderUpdate);
                    Toast.makeText(context, "Đã xác nhận giao hàng thành công", Toast.LENGTH_SHORT).show();
                });
            }

            builder.setNegativeButton("Hủy", null);
            builder.show();
        } else {
            // User xác nhận nhận hàng (giữ nguyên code cũ)
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setIcon(R.drawable.thongbao);
            builder.setTitle("Thông báo");
            builder.setMessage("Bạn chắc chắn đã nhận được hàng?");
            builder.setPositiveButton("Đúng", (dialog, which) -> {
                Order orderUpdate = new Order();
                orderUpdate.set_id(order.get_id());
                orderUpdate.setStatus("Đã giao hàng");
                updateOrder(orderUpdate);

                Intent intent = new Intent(context, DanhGia.class);
                intent.putExtra("orderId", order.get_id());
                intent.putExtra("listProductInOrder", new ArrayList<>(order.getProducts()));
                context.startActivity(intent);
            });
            builder.setNegativeButton("Hủy", null);
            builder.show();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    private void showReorderDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_shopping_cart);
        builder.setTitle("Mua lại");
        builder.setMessage("Bạn có muốn thêm tất cả sản phẩm từ đơn hàng này vào giỏ hàng?");
        builder.setPositiveButton("Đồng ý", (dialog, which) -> reorderProducts(order));
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // Chức năng mua lại - thêm sản phẩm vào giỏ hàng
    private void reorderProducts(Order order) {
        if (!isValidUser()) {
            Toast.makeText(context, "Không thể xác định người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ProductInOrder> products = order.getProducts();
        if (products == null || products.isEmpty()) {
            Toast.makeText(context, "Đơn hàng không có sản phẩm để mua lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Bắt đầu mua lại đơn hàng: " + order.get_id() + " với " + products.size() + " sản phẩm");
        addProductsToCartBatch(products);
    }

    private boolean isValidUser() {
        return userId != null && !userId.isEmpty();
    }

    private void addProductsToCartBatch(List<ProductInOrder> products) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger processedCount = new AtomicInteger(0);
        int totalProducts = products.size();

        Log.d(TAG, "Thêm " + totalProducts + " sản phẩm vào giỏ hàng cho user: " + userId);

        for (int i = 0; i < products.size(); i++) {
            ProductInOrder productInOrder = products.get(i);
            Cart cartItem = createCartItem(productInOrder);

            if (cartItem == null) {
                Log.e(TAG, "Không thể tạo cart item cho sản phẩm " + (i + 1));
                failCount.incrementAndGet();
                checkCompletionAndShowResult(processedCount.incrementAndGet(), totalProducts, successCount.get(), failCount.get());
                continue;
            }

            Log.d(TAG, "Đang thêm sản phẩm " + (i + 1) + ": " + new Gson().toJson(cartItem));

            ApiService apiService = ApiClient.getApiService();
            Call<Cart> call = apiService.addCart(cartItem);

            call.enqueue(new Callback<Cart>() {
                @Override
                public void onResponse(Call<Cart> call, Response<Cart> response) {
                    if (response.isSuccessful()) {
                        successCount.incrementAndGet();
                        Log.d(TAG, "Thêm sản phẩm thành công");
                    } else {
                        failCount.incrementAndGet();
                        Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error Body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                    }
                    checkCompletionAndShowResult(processedCount.incrementAndGet(), totalProducts, successCount.get(), failCount.get());
                }

                @Override
                public void onFailure(Call<Cart> call, Throwable t) {
                    failCount.incrementAndGet();
                    Log.e(TAG, "Network Error: " + t.getMessage());
                    checkCompletionAndShowResult(processedCount.incrementAndGet(), totalProducts, successCount.get(), failCount.get());
                }
            });
        }
    }

    private void checkCompletionAndShowResult(int processed, int total, int success, int fail) {
        if (processed >= total) {
            String message;
            if (success > 0) {
                message = "Đã thêm " + success + " sản phẩm vào giỏ hàng thành công!";
                if (fail > 0) {
                    message += " (" + fail + " sản phẩm thất bại)";
                }
            } else {
                message = "Không thể thêm sản phẩm nào vào giỏ hàng";
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Hoàn thành mua lại: " + success + " thành công, " + fail + " thất bại");
        }
    }

    private Cart createCartItem(ProductInOrder productInOrder) {
        try {
            Product product = productInOrder.getId_product();
            if (product == null) {
                Log.e(TAG, "Product is null in ProductInOrder");
                return null;
            }

            Cart cartItem = new Cart();
            cartItem.setIdUser(userId);
            cartItem.setIdProduct(product);
            cartItem.setQuantity(productInOrder.getQuantity());
            cartItem.setPrice(productInOrder.getPrice());

            try {
                String sizeStr = productInOrder.getSize();
                if (sizeStr != null && !sizeStr.isEmpty()) {
                    cartItem.setSize(Integer.parseInt(sizeStr));
                } else {
                    cartItem.setSize(0);
                }
            } catch (NumberFormatException e) {
                cartItem.setSize(0);
            }

            cartItem.setColor(productInOrder.getColor());
            cartItem.setTotal(productInOrder.getPrice() * productInOrder.getQuantity());
            cartItem.setStatus(1);

            String imageUrl = getCorrectProductImage(productInOrder);
            cartItem.setImg_cart(imageUrl);

            return cartItem;
        } catch (Exception e) {
            Log.e(TAG, "Error creating cart item", e);
            return null;
        }
    }

    private String getCorrectProductImage(ProductInOrder productInOrder) {
        String imageUrl = "";

        try {
            Product product = productInOrder.getId_product();
            if (product == null) {
                Log.e(TAG, "Product is null in ProductInOrder");
                return "";
            }

            String selectedColorName = productInOrder.getColor();
            String selectedSize = productInOrder.getSize();

            String selectedImageUrl = product.getAvt_imgproduct();

            if (product.getVariations() != null && !product.getVariations().isEmpty()) {
                for (Variation variant : product.getVariations()) {
                    if (variant.getColor() != null &&
                            variant.getColor().getName().equals(selectedColorName) &&
                            String.valueOf(variant.getSize()).equals(selectedSize)) {

                        if (variant.getList_imgproduct() != null && !variant.getList_imgproduct().isEmpty()) {
                            selectedImageUrl = ApiClient.IMAGE_URL + variant.getList_imgproduct().get(0);
                            break;
                        }
                    }
                }
            }

            if (selectedImageUrl == null || selectedImageUrl.trim().isEmpty()) {
                if (product.getAvt_imgproduct() != null && !product.getAvt_imgproduct().trim().isEmpty()) {
                    selectedImageUrl = ApiClient.IMAGE_URL + product.getAvt_imgproduct();
                } else if (product.getList_imgproduct() != null && !product.getList_imgproduct().isEmpty()) {
                    selectedImageUrl = ApiClient.IMAGE_URL + product.getList_imgproduct().get(0);
                } else {
                    selectedImageUrl = "";
                }
            }

            imageUrl = selectedImageUrl;

        } catch (Exception e) {
            Log.e(TAG, "Error getting correct product image", e);
            try {
                Product product = productInOrder.getId_product();
                if (product != null && product.getAvt_imgproduct() != null) {
                    imageUrl = ApiClient.IMAGE_URL + product.getAvt_imgproduct();
                }
            } catch (Exception fallbackError) {
                Log.e(TAG, "Error in fallback image selection", fallbackError);
            }
        }

        return imageUrl;
    }

    private String formatDateForUser(String isoDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = inputFormat.parse(isoDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting user date: " + isoDate, e);
            return isoDate;
        }
    }

    private String formatDateForStaff(String isoDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = inputFormat.parse(isoDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting staff date: " + isoDate, e);
            return isoDate;
        }
    }

    private void updateOrder(Order order) {
        ApiService apiService = ApiClient.getApiService();
        Call<Order> call = apiService.upStatus(order.get_id(), order);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                Log.d("OrderAdapter", "Update order response: " + response.code() + " - " + response.message());
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                    Log.d("OrderAdapter", "Update successful, scheduling refresh after delay...");
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        if (onOrderCancelled != null) {
                            Log.d("OrderAdapter", "Executing refresh callback...");
                            onOrderCancelled.run();
                        } else {
                            Log.d("OrderAdapter", "Callback is null, using notifyDataSetChanged");
                            notifyDataSetChanged();
                        }
                    }, 1000);
                } else {
                    Toast.makeText(context, "Cập nhật thất bại: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Log.e(TAG, "Error updating order", t);
                Log.d("OrderAdapter", "Update order failed: " + t.getMessage());
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelOrder(Order order, int position) {
        ApiService apiService = ApiClient.getApiService();
        apiService.upStatus(order.get_id(), order).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Hủy đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    ordList.remove(position);
                    notifyItemRemoved(position);
                    if (onOrderCancelled != null) {
                        onOrderCancelled.run();
                    }
                } else {
                    Toast.makeText(context, "Hủy đơn thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Log.e(TAG, "Error cancelling order", t);
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return ordList != null ? ordList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvmaDH, tvthanhTien, tvngayMua, tvTT, tvLydo, tvSoLuongSP, tvTimeUp;
        Button btnHuy, btnChiTiet, btnNhan, btnMuaLai;
        View layoutCustomerInfo;
        TextView tvCustomerName, tvCustomerPhone, tvCustomerAddress;
        ImageView btnCopyPhone, btnCopyAddress;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvmaDH = itemView.findViewById(R.id.tvmaDH);
            tvthanhTien = itemView.findViewById(R.id.tvthanhTien);
            tvSoLuongSP = itemView.findViewById(R.id.tvSoLuongSP);
            tvngayMua = itemView.findViewById(R.id.tvngayMua);
            tvTT = itemView.findViewById(R.id.tvTT);
            tvTimeUp = itemView.findViewById(R.id.tvTimeUp);
            tvLydo = itemView.findViewById(R.id.tvLydo);
            btnHuy = itemView.findViewById(R.id.btnHuy);
            btnChiTiet = itemView.findViewById(R.id.btnChitiet);
            btnNhan = itemView.findViewById(R.id.btnNhan);
            btnMuaLai = itemView.findViewById(R.id.btnMuaLai);
            layoutCustomerInfo = itemView.findViewById(R.id.layoutCustomerInfo);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvCustomerAddress = itemView.findViewById(R.id.tvCustomerAddress);
            btnCopyPhone = itemView.findViewById(R.id.btnCopyPhone);
            btnCopyAddress = itemView.findViewById(R.id.btnCopyAddress);
        }
    }

    private boolean isZaloPayPaid(String payInfo) {
        if (payInfo == null) return false;
        String normalized = payInfo.trim().toLowerCase(Locale.getDefault());
        return normalized.contains("zalopay") && (normalized.contains("đã thanh toán") || normalized.contains("da thanh toan") || normalized.contains("paid"));
    }
}