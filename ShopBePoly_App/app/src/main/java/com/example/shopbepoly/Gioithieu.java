package com.example.shopbepoly;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Gioithieu extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gioithieu);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnClearCache = findViewById(R.id.btnClearCache);

        btnBack.setOnClickListener(v -> finish());
        btnClearCache.setOnClickListener(v -> {
            // TODO: Thực hiện xóa cache thực tế nếu muốn
            Toast.makeText(this, "Đã xóa bộ nhớ đệm!", Toast.LENGTH_SHORT).show();
        });
    }
}