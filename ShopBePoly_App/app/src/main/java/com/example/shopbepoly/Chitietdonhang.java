package com.example.shopbepoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.shopbepoly.Screen.DanhGia;
import com.squareup.picasso.Picasso;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.util.List;

public class Chitietdonhang extends AppCompatActivity {
    private String orderId;
    private String status;
    private TextView txtTT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chitietdonhang);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        orderId = intent.getStringExtra("order_id");
        status = intent.getStringExtra("order_status");
        String address = intent.getStringExtra("order_address");
        double bill = intent.getDoubleExtra("order_bill", 0);
        String date = intent.getStringExtra("order_date");
        String pay = intent.getStringExtra("order_pay");
        String nameproduct = intent.getStringExtra("order_nameproduct");
        String imageJson = intent.getStringExtra("order_image");
        String quantity = intent.getStringExtra("order_quantity");

        // Ánh xạ view
        TextView txtMaDH = findViewById(R.id.txtmaDH);
        txtTT = findViewById(R.id.txtTT);
        TextView txtDiaChi = findViewById(R.id.txtdiaChi);
        TextView txtSoLuong = findViewById(R.id.txtsoLuong);
        TextView txtThanhTien = findViewById(R.id.txtthanhTien);
        TextView txtNgayMua = findViewById(R.id.txtngayMua);
        TextView txtPay = findViewById(R.id.txtPay);
        TextView txtTenSP = findViewById(R.id.txtenSP);
        ImageView imgSP = findViewById(R.id.imgSP);
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnDaNhanHang = findViewById(R.id.btnXacNhan);

        // Set dữ liệu lên view
        txtMaDH.setText(orderId != null ? orderId : "N/A");
        txtTT.setText(status != null ? status : "N/A");
        txtDiaChi.setText(address != null ? address : "N/A");
        txtNgayMua.setText(date != null ? date : "N/A");
        txtPay.setText(pay != null ? pay : "N/A");
        txtTenSP.setText(nameproduct != null ? nameproduct : "N/A");
        txtSoLuong.setText(quantity != null ? quantity : "N/A");

        // Format tiền
        DecimalFormat formatter = new DecimalFormat("#,###");
        txtThanhTien.setText(formatter.format(bill) + " đ");

        // Load ảnh sản phẩm
        try {
            List<String> imageList = new Gson().fromJson(imageJson, new TypeToken<List<String>>(){}.getType());
            if (imageList != null && !imageList.isEmpty()) {
                Picasso.get().load(imageList.get(0)).placeholder(R.drawable.avatar_default).into(imgSP);
            } else {
                imgSP.setImageResource(R.drawable.avatar_default);
            }
        } catch (Exception e) {
            Log.e("ChiTietDonHang", "Lỗi parse ảnh", e);
            imgSP.setImageResource(R.drawable.avatar_default);
        }

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Xác nhận "Đã nhận hàng"
        if ("Đang xử lý".equalsIgnoreCase(status)) {
            btnDaNhanHang.setVisibility(Button.VISIBLE);
            btnDaNhanHang.setOnClickListener(v -> {
                status = "Đã nhận";
                txtTT.setText(status);
                updateStatusInLocal(orderId, status);
                Toast.makeText(this, "Cập nhật trạng thái đơn hàng thành 'Đã nhận'", Toast.LENGTH_SHORT).show();
                btnDaNhanHang.setEnabled(false);

                // Chuyển sang màn đánh giá
                Intent danhGiaIntent = new Intent(Chitietdonhang.this, DanhGia.class);
                danhGiaIntent.putExtra("order_id", orderId);
                danhGiaIntent.putExtra("order_nameproduct", nameproduct);
                startActivity(danhGiaIntent);
            });
        } else {
            btnDaNhanHang.setVisibility(Button.GONE);
        }
    }

    private void updateStatusInLocal(String id, String newStatus) {
        SharedPreferences prefs = getSharedPreferences("OrderPrefs", MODE_PRIVATE);
        String ordersJson = prefs.getString("orders_list", "[]");

        List<com.example.shopbepoly.DTO.Order> orderList = new Gson()
                .fromJson(ordersJson, new TypeToken<List<com.example.shopbepoly.DTO.Order>>(){}.getType());

        boolean found = false;
        for (com.example.shopbepoly.DTO.Order order : orderList) {
            if (order.get_id().equals(id)) {
                order.setStatus(newStatus);
                found = true;
                break;
            }
        }

        if (found) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("orders_list", new Gson().toJson(orderList));
            editor.apply();
        }
    }
}