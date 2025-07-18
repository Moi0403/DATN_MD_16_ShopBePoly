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

import com.example.shopbepoly.Adapter.ProOrderAdapter;
import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.Screen.DanhGia;
import com.squareup.picasso.Picasso;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Chitietdonhang extends AppCompatActivity {
    private String orderId;
    private String status;
    private TextView txtmaDH, txtTT, txtDC, txtSL, txtTTien, txtDay, txtPay;
    private RecyclerView rc_orderpro;
    private Order order;
    private ProOrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chitietdonhang);
        txtmaDH = findViewById(R.id.txtMaDH);
        txtTT = findViewById(R.id.txtTT);
        txtDC = findViewById(R.id.txtDC);
        txtSL = findViewById(R.id.txtSL);
        txtTTien = findViewById(R.id.txtTTien);
        txtDay = findViewById(R.id.txtDay);
        txtPay = findViewById(R.id.txtPay);
        rc_orderpro = findViewById(R.id.rc_orderPro);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        order = (Order) getIntent().getSerializableExtra("order");

        if (order != null) {
            txtmaDH.setText(order.get_id()+"");
            txtTT.setText(order.getStatus()+"");
            txtDC.setText(order.getAddress()+"");
            txtSL.setText(order.getQuantity_order()+"");
            txtTTien.setText(formatCurrency(Integer.parseInt(order.getTotal()))+"");
            txtDay.setText(formatDate(order.getDate())+"");
            txtPay.setText(order.getPay()+"");
        } else {
            Toast.makeText(this, "Không nhận được đơn hàng", Toast.LENGTH_SHORT).show();
        }

        rc_orderpro.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new ProOrderAdapter(Chitietdonhang.this, order.getProducts());
        rc_orderpro.setAdapter(orderAdapter);

    }

    private String formatCurrency(int amount) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        return formatter.format(amount) + " đ";
    }
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdfInput.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdfInput.parse(dateStr);

            SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
            sdfOutput.setTimeZone(TimeZone.getDefault());
            return sdfOutput.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }


}
