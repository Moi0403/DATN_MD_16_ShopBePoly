package com.example.shopbepoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Screen.LoginScreen;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoiMatKhau extends AppCompatActivity {
    private EditText edtOldPassword, edtNewPassword, edtConfirmPassword;
    private TextInputLayout layoutOldPassword;
    private Button btnSave;
    private ImageView btnBack;
    private boolean isForgotPassword = false;
    private String emailFromIntent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doimatkhau);

        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        layoutOldPassword = findViewById(R.id.layoutOldPassword);
        btnSave = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBackdmk);

        Intent intent = getIntent();
        isForgotPassword = intent.getBooleanExtra("isForgotPassword", false);
        emailFromIntent = intent.getStringExtra("email");

        if (isForgotPassword) {
            layoutOldPassword.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(v -> onBackPressed());

        btnSave.setOnClickListener(v -> {
            String oldPass = edtOldPassword.getText().toString().trim();
            String newPass = edtNewPassword.getText().toString().trim();
            String confirmPass = edtConfirmPassword.getText().toString().trim();

            if (!isForgotPassword && oldPass.isEmpty()) {
                edtOldPassword.setError("Vui lòng nhập mật khẩu cũ");
                return;
            }

            if (newPass.isEmpty()) {
                edtNewPassword.setError("Vui lòng nhập mật khẩu mới");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
                return;
            }

            changePassword(oldPass, newPass);
        });
    }

    private void changePassword(String oldPass, String newPass) {
        ApiService apiService = ApiClient.getApiService();

        try {
            JSONObject json = new JSONObject();
            Call<ResponseBody> call;

            if (isForgotPassword) {
                json.put("email", emailFromIntent);
                json.put("newPassword", newPass);
                RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
                call = apiService.resetPasswordByEmail(body);
            } else {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String userId = sharedPreferences.getString("userId", "");
                json.put("oldPassword", oldPass);
                json.put("newPassword", newPass);
                RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
                call = apiService.changePassword(userId, body);
            }

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        showToast("Đổi mật khẩu thành công");
                        if (isForgotPassword) {
                            startActivity(new Intent(DoiMatKhau.this, LoginScreen.class));
                            finish();
                        } else {
                            finish();
                        }
                    } else {
                        showToast("Đổi mật khẩu thất bại");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    showToast("Lỗi kết nối máy chủ");
                }
            });
        } catch (Exception e) {
            showToast("Lỗi xử lý dữ liệu");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
