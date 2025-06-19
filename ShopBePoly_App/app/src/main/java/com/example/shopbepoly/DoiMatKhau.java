package com.example.shopbepoly;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONObject;

public class DoiMatKhau extends AppCompatActivity {

    EditText edtOldPassword, edtNewPassword, edtConfirmPassword;
    Button btnUpdate;
    ImageView btnback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.doimatkhau);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnback = findViewById(R.id.btnBack);

        btnback.setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> {
            String oldPass = edtOldPassword.getText().toString().trim();
            String newPass = edtNewPassword.getText().toString().trim();
            String confirmPass = edtConfirmPassword.getText().toString().trim();
            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", "");
            ApiService apiService = ApiClient.getApiService();
            try {
                JSONObject json = new JSONObject();
                json.put("oldPassword", oldPass);
                json.put("newPassword", newPass);
                RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
                apiService.changePassword(userId, body).enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String resp = response.body().string();
                                JSONObject obj = new JSONObject(resp);
                                String msg = obj.optString("message", "Đổi mật khẩu thành công");
                                Toast.makeText(DoiMatKhau.this, msg, Toast.LENGTH_SHORT).show();
                                finish();
                            } catch (Exception e) {
                                Toast.makeText(DoiMatKhau.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            String errorMsg = "Đổi mật khẩu thất bại";
                            if (response.errorBody() != null) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    android.util.Log.e("DoiMatKhau", "API error body: " + errorBody);
                                    // Thử parse JSON, nếu không phải thì log ra text
                                    try {
                                        JSONObject obj = new JSONObject(errorBody);
                                        errorMsg = obj.optString("message", errorMsg);
                                    } catch (Exception jsonEx) {
                                        // Nếu không phải JSON, dùng luôn errorBody làm message
                                        errorMsg = errorBody;
                                    }
                                } catch (Exception e) {
                                    errorMsg += " (không đọc được lỗi chi tiết)";
                                    android.util.Log.e("DoiMatKhau", "Không đọc được error body", e);
                                }
                            }
                            Toast.makeText(DoiMatKhau.this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(DoiMatKhau.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        android.util.Log.e("DoiMatKhau", "API changePassword onFailure", t);
                    }
                });
            } catch (Exception e) {
                Toast.makeText(DoiMatKhau.this, "Lỗi tạo dữ liệu đổi mật khẩu", Toast.LENGTH_SHORT).show();
                android.util.Log.e("DoiMatKhau", "Lỗi tạo JSON body", e);
            }
        });
    }
}