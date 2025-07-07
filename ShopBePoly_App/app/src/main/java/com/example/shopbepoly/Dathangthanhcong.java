package com.example.shopbepoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shopbepoly.Screen.ChiTietSanPham;
import com.example.shopbepoly.fragment.HomeFragment;

public class Dathangthanhcong extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dathangthanhcong);

        Button btnQuayLai = findViewById(R.id.btnQuayLai);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnQuayLai.setOnClickListener(v -> {
            Intent intent = new Intent(Dathangthanhcong.this, Donhang.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
//        btnQuayLai.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Dathangthanhcong.this, HomeFragment.class);
//                startActivity(intent);
//            }
//        });
        btnBack.setOnClickListener(v -> finish());
    }
}