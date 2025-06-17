package com.example.shopbepoly.Screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.User;
import com.example.shopbepoly.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterScreen extends AppCompatActivity {

    private TextView tvLogin;
    private EditText edtName, edtEmail, edtPhone, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private CheckBox checkBoxRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        checkBoxRegister = findViewById(R.id.checkBoxRegister);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        ImageView btnTogglePassword = findViewById(R.id.btnTogglePassword);
        ImageView btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

        final boolean[] isPasswordVisible = {false};
        final boolean[] isConfirmPasswordVisible = {false};

        btnTogglePassword.setOnClickListener(v ->
                togglePasswordVisibility(edtPassword, btnTogglePassword, isPasswordVisible)
        );

        btnToggleConfirmPassword.setOnClickListener(v ->
                togglePasswordVisibility(edtConfirmPassword, btnToggleConfirmPassword, isConfirmPasswordVisible)
        );

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterScreen.this, LoginScreen.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!email.contains("@")) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!phone.matches("^0\\d{8,10}$")) {
                Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!checkBoxRegister.isChecked()) {
                Toast.makeText(this, "Bạn cần đồng ý điều khoản sử dụng", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User("", name, email, phone, password, "", 0);
            ApiService apiService = ApiClient.getApiService();
            Call<Void> call = apiService.register(user);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(RegisterScreen.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterScreen.this, LoginScreen.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterScreen.this, "Đăng ký thất bại: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(RegisterScreen.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleButton, boolean[] isVisible) {
        // Lưu font hiện tại để tránh bị đổi font sau khi thay đổi inputType
        int selection = editText.getText().length();
        editText.setTypeface(editText.getTypeface());

        if (isVisible[0]) {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleButton.setImageResource(R.drawable.ic_eye_on);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleButton.setImageResource(R.drawable.ic_eye_off);
        }
        editText.setTypeface(editText.getTypeface()); // Giữ nguyên font
        editText.setSelection(selection);
        isVisible[0] = !isVisible[0];
    }
}