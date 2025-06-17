package com.example.shopbepoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class LienHe extends AppCompatActivity {

    ImageButton btnBack, btnMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lien_he);

        btnBack = findViewById(R.id.btnBack);
        btnMessage = findViewById(R.id.btnMessage);

        btnBack.setOnClickListener(v -> finish());

        btnMessage.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            Map<String, ?> allPrefs = preferences.getAll();
            for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                Log.d("ALL_PREFS", entry.getKey() + " = " + entry.getValue());
            }


            String userId = preferences.getString("userId", null);

            if (userId != null) {
                Intent intent = new Intent(LienHe.this, Chat.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Bạn cần đăng nhập trước khi nhắn tin", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
