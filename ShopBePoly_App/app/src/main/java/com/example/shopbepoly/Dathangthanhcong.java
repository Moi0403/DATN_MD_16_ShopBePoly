package com.example.shopbepoly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

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
        btnBack.setOnClickListener(v -> finish());
    }
}