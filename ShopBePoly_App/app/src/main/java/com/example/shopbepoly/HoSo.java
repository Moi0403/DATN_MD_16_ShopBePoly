package com.example.shopbepoly;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HoSo extends AppCompatActivity {
    TextView txtDangXuat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ho_so);
        txtDangXuat = findViewById(R.id.txtDangXuat);
        txtDangXuat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExit();
            }
        });

        }
    private void showExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HoSo.this);
        builder.setTitle("Thông báo!");
        builder.setIcon(R.drawable.bell);
        builder.setMessage("Bạn chắc chắn muốn đăng xuất không?");
        builder.setCancelable(false);

        builder.setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(HoSo.this, DieuKhoanvaDieuKien.class));
                finish();
            }
        });
        builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}