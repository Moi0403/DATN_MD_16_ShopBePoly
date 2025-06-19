package com.example.shopbepoly;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Lichsugiaodich extends AppCompatActivity {
    ImageButton btnBack;
    ImageView imageSanpham;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lichsugiaodich);
        btnBack = findViewById(R.id.btnBack);
        imageSanpham = findViewById(R.id.imageSanpham);


        imageSanpham.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Lichsugiaodich.this, Chitietlichsugiaodich.class);
                startActivity(intent);
            }
        });
        btnBack.setOnClickListener(v -> finish());
    }
}