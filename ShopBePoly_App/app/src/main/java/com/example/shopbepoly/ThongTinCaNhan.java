package com.example.shopbepoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.User;
import com.bumptech.glide.Glide;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ThongTinCaNhan extends AppCompatActivity {
    private static final int REQUEST_CODE_UPDATE = 1001;

    ImageButton btnBack;
    Button btnUpdate;
    TextView txtUser, txtGioitinh, txtNgaysinh, txtSodienthoai, txtEmailTTCN;
    ImageView imgAvatar;
    User currentUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_ca_nhan);
        btnBack = findViewById(R.id.btnBack);
        btnUpdate = findViewById(R.id.btnUpdate);
        txtUser = findViewById(R.id.txtUser);
        txtGioitinh = findViewById(R.id.txtGioitinh);
        txtNgaysinh = findViewById(R.id.txtNgaysinh);
        txtSodienthoai = findViewById(R.id.txtSodienthoai);
        txtEmailTTCN = findViewById(R.id.txtEmailTTCN);
        imgAvatar = findViewById(R.id.imgAvatar);

        btnBack.setOnClickListener(v -> finish());
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    Intent intent = new Intent(ThongTinCaNhan.this, SuaThongTinCaNhan.class);
                    intent.putExtra("name", currentUser.getName());
                    intent.putExtra("birthday", currentUser.getBirthday());
                    intent.putExtra("phone", currentUser.getPhone_number());
                    intent.putExtra("email", currentUser.getEmail());
                    // Nếu có avatar thì truyền thêm
                    startActivityForResult(intent, REQUEST_CODE_UPDATE);
                }
            }
        });

        loadUserInfo();
    }

    private void loadUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        android.util.Log.d("UserInfo", "Loading info for userId: " + userId);

        ApiService apiService = ApiClient.getApiService();
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                android.util.Log.d("UserInfo", "API Response received. Success: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    boolean found = false;
                    for (User user : response.body()) {
                        android.util.Log.d("UserInfo", "API User: id=" + user.getId() + ", name=" + user.getName());
                        if (user.getId().equals(userId)) {
                            currentUser = user;
                            found = true;
                            android.util.Log.d("UserInfo", "User found. Name: " + user.getName() + 
                                ", Email: " + user.getEmail() + 
                                ", Phone: " + user.getPhone_number() + 
                                ", Birthday: " + user.getBirthday() + 
                                ", Gender: " + user.getGender());
                            txtUser.setText(user.getName() != null ? user.getName() : "");
                            txtEmailTTCN.setText(user.getEmail() != null ? user.getEmail() : "");
                            txtSodienthoai.setText(user.getPhone_number() != null ? user.getPhone_number() : "");
                            txtGioitinh.setText(user.getGender() != null ? user.getGender() : "");
                            String birthday = user.getBirthday();
                            if (birthday != null && !birthday.isEmpty()) {
                                try {
                                    SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    Date date = sdfInput.parse(birthday);
                                    txtNgaysinh.setText(sdfOutput.format(date));
                                } catch (Exception e) {
                                    txtNgaysinh.setText(birthday);
                                }
                            } else {
                                txtNgaysinh.setText("");
                            }
                            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                                android.util.Log.d("AvatarURL", "Avatar URL: " + user.getAvatar());
                                Glide.with(ThongTinCaNhan.this).load(user.getAvatar()).placeholder(R.drawable.avatar_default).error(R.drawable.avatar_default).into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(R.drawable.avatar_default);
                            }
                            break;
                        }
                    }
                    if (!found) {
                        android.util.Log.e("UserInfo", "Không tìm thấy userId này trong danh sách trả về từ API!");
                        android.util.Log.e("UserInfo", "Danh sách userId trả về: " + getAllUserIds(response.body()));
                    }
                } else {
                    android.util.Log.e("UserInfo", "API trả về lỗi hoặc body null. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            android.util.Log.e("UserInfo", "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            android.util.Log.e("UserInfo", "Không đọc được error body");
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                android.util.Log.e("UserInfo", "Lỗi kết nối: " + t.getMessage(), t);
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(ThongTinCaNhan.this, 
                        "Không thể tải thông tin: " + t.getMessage(), 
                        android.widget.Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Hàm phụ để log danh sách userId trả về
    private String getAllUserIds(List<User> users) {
        StringBuilder sb = new StringBuilder();
        for (User u : users) {
            sb.append(u.getId()).append(", ");
        }
        return sb.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPDATE && resultCode == Activity.RESULT_OK) {
            // Refresh user info when returning from SuaThongTinCaNhan
            loadUserInfo();
        }
    }
}