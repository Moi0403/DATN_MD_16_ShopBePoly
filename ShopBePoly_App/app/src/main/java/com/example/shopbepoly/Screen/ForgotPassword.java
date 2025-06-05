package com.example.shopbepoly.Screen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopbepoly.R;

public class ForgotPassword extends AppCompatActivity {

    private ImageView ivBack;
    private EditText edtEmail;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        ivBack = findViewById(R.id.ivBack);
        edtEmail = findViewById(R.id.edtEmail);
        btnConfirm = findViewById(R.id.btnConfirm);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
