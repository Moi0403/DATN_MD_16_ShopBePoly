package com.example.shopbepoly;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.ProOrderAdapter;
import com.example.shopbepoly.DTO.Order;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chitietdonhang extends AppCompatActivity {
    private String orderId;
    private TextView txtmaDH, txtTT, txtDC, txtSL, txtTTien, txtDay, txtPay;
    private RecyclerView rc_orderpro;
    private Order order;
    private ProOrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chitietdonhang);

        // Ánh xạ
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

        boolean fromNotification = getIntent().getBooleanExtra("fromNotification", false);
        orderId = getIntent().getStringExtra("orderId");

        if (fromNotification && orderId != null) {
            fetchOrderById(orderId); // Lấy đơn hàng từ server theo ID
        } else {
            order = (Order) getIntent().getSerializableExtra("order");
            if (order != null) {
                setDataToViews(order);
            } else {
                Toast.makeText(this, "Không nhận được đơn hàng", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchOrderById(String id) {
        ApiService apiService = ApiClient.getApiService();
        Call<Order> call = apiService.getOrderDetail(id);

        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    order = response.body();
                    setDataToViews(order);
                } else {
                    Toast.makeText(Chitietdonhang.this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(Chitietdonhang.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                Log.e("ORDER_DETAIL", "Error: " + t.getMessage());
            }
        });
    }

    private void setDataToViews(Order order) {
        txtmaDH.setText(order.get_id());
        txtTT.setText(order.getStatus());
        txtDC.setText(order.getAddress());
        txtSL.setText(String.valueOf(order.getQuantity_order()));
        txtTTien.setText(formatCurrency(Integer.parseInt(order.getTotal())));
        txtDay.setText(formatDate(order.getDate()));
        txtPay.setText(order.getPay());

        rc_orderpro.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new ProOrderAdapter(this, order.getProducts());
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
