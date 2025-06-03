package com.example.shopbepoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class Profile extends AppCompatActivity {

    TextView txtThongtincanhan, txtLichsugiaodich, txtGioiThieu, txtDoimatkhau, txtLienhe, txtDieuKhoan, txtChinhsach;
    ImageButton btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        txtThongtincanhan = findViewById(R.id.txtThongtincanhan);
        txtLichsugiaodich = findViewById(R.id.txtLichsugiaodich);
        txtGioiThieu = findViewById(R.id.txtGioithieu);
        txtDoimatkhau = findViewById(R.id.txtDoimatkhau);
        txtLienhe = findViewById(R.id.txtLienhe);
        txtDieuKhoan = findViewById(R.id.txtDieukhoan);
        txtChinhsach = findViewById(R.id.txtChinhsach);
        btnBack = findViewById(R.id.btnBack);

        txtThongtincanhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, ThongTinCaNhan.class);
                startActivity(intent);
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, MainActivity.class);
                startActivity(intent);
            }
        });
        txtLichsugiaodich.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, Lichsugiaodich.class);
            startActivity(intent);
        });



    }

}