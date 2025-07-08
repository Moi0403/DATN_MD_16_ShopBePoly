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

import org.json.JSONArray;

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
        orderId = getIntent().getStringExtra("order_id");
        status = getIntent().getStringExtra("order_status");
        String address = getIntent().getStringExtra("order_address");
        String bill = getIntent().getStringExtra("order_bill");
        String date = getIntent().getStringExtra("order_date");
        String pay = getIntent().getStringExtra("order_pay");
        String nameproduct = getIntent().getStringExtra("order_nameproduct");
        String imageUrl = getIntent().getStringExtra("order_image");

        // Ánh xạ view
        TextView txtMaDH = findViewById(R.id.txtmaDH);
        txtTT = findViewById(R.id.txtTT);
        TextView txtDiaChi = findViewById(R.id.txtdiaChi);
        TextView txtThanhTien = findViewById(R.id.txtthanhTien);
        TextView txtNgayMua = findViewById(R.id.txtngayMua);
        TextView txtPay = findViewById(R.id.txtPay);
        TextView txtTenSP = findViewById(R.id.txtenSP);
        ImageView imgSP = findViewById(R.id.imgSP);
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnDaNhanHang = findViewById(R.id.btnXacNhan);

        // Set dữ liệu lên view
        txtMaDH.setText(orderId);
        txtTT.setText(status);
        txtDiaChi.setText(address);

        // Format tiền
        try {
            DecimalFormat formatter = new DecimalFormat("#,###");
            int billInt = Integer.parseInt(bill.replaceAll("[^\\d]", ""));
            txtThanhTien.setText(formatter.format(billInt) + " đ");
        } catch (Exception e) {
            txtThanhTien.setText(bill);
        }

        txtNgayMua.setText(date);
        txtPay.setText(pay);
        txtTenSP.setText(nameproduct);

        // Xử lý image
//        if (imageUrl != null && !imageUrl.isEmpty()) {
//            if (imageUrl.startsWith("[") && imageUrl.endsWith("]")) {
//                // Tách lấy ảnh đầu tiên
//                imageUrl = imageUrl.replace("[", "").replace("]", "").replace("\"", "").split(",")[0].trim();
//            }
//            Picasso.get().load(imageUrl).placeholder(R.drawable.avatar_default).into(imgSP);
//        } else {
//            imgSP.setImageResource(R.drawable.avatar_default);
//        }
        // Xử lý image
        String imageJson = getIntent().getStringExtra("order_image");

        try {
            List<String> imageList = new Gson().fromJson(imageJson, new com.google.gson.reflect.TypeToken<List<String>>(){}.getType());

            if (imageList != null && !imageList.isEmpty()) {
                Picasso.get().load(imageList.get(0)).placeholder(R.drawable.avatar_default).into(imgSP);
            } else {
                imgSP.setImageResource(R.drawable.avatar_default);
            }
        } catch (Exception e) {
            Log.e("ChiTietDonHang", "Lỗi parse ảnh", e);
            imgSP.setImageResource(R.drawable.avatar_default);
        }

        // Nút back
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

                //Chuyển sang màn đánh giá sau khi cập nhật trạng thái
                Intent intent = new Intent(Chitietdonhang.this, DanhGia.class);

                // Nếu muốn truyền thêm dữ liệu sang màn DanhGia (ví dụ: orderId, productName...)
                intent.putExtra("order_id", orderId);
                intent.putExtra("order_nameproduct", getIntent().getStringExtra("order_nameproduct"));

                startActivity(intent);
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