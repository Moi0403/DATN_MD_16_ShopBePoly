package com.example.shopbepoly;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ImageButton;
import com.squareup.picasso.Picasso;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Chitietdonhang extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chitietdonhang);

        // Nhận dữ liệu từ Intent
        String id = getIntent().getStringExtra("order_id");
        String status = getIntent().getStringExtra("order_status");
        String address = getIntent().getStringExtra("order_address");
        String bill = getIntent().getStringExtra("order_bill");
        String date = getIntent().getStringExtra("order_date");
        String pay = getIntent().getStringExtra("order_pay");
        String nameproduct = getIntent().getStringExtra("order_nameproduct");
        String imageUrl = getIntent().getStringExtra("order_image");

        // Ánh xạ view
        TextView txtMaDH = findViewById(R.id.txtmaDH);
        TextView txtTT = findViewById(R.id.txtTT);
        TextView txtDiaChi = findViewById(R.id.txtdiaChi);
        TextView txtThanhTien = findViewById(R.id.txtthanhTien);
        TextView txtNgayMua = findViewById(R.id.txtngayMua);
        TextView txtPay = findViewById(R.id.txtPay);
        TextView txtTenSP = findViewById(R.id.txtenSP);
        ImageView imgSP = findViewById(R.id.imgSP);
        ImageButton btnBack = findViewById(R.id.btnBack);
        // Nếu có số lượng thì thêm:
        // TextView txtSoLuong = findViewById(R.id.txtsoLuong);

        // Set dữ liệu lên view
        txtMaDH.setText(id);
        txtTT.setText(status);
        txtDiaChi.setText(address);
        txtThanhTien.setText(bill);
        txtNgayMua.setText(date);
        txtPay.setText(pay);
        txtTenSP.setText(nameproduct);
        // txtSoLuong.setText(quantity);

        // Hiển thị ảnh sản phẩm
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.avatar_default).into(imgSP);
        } else {
            imgSP.setImageResource(R.drawable.avatar_default);
        }

        // Xử lý nút back
        btnBack.setOnClickListener(v -> finish());
    }
}