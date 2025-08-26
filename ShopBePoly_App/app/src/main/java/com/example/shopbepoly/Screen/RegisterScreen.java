package com.example.shopbepoly.Screen;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.CheckEmailResponse;
import com.example.shopbepoly.DTO.CheckUsernameResponse;
import com.example.shopbepoly.DieuKhoanvaDieuKien;
import com.example.shopbepoly.R;
import com.example.shopbepoly.VerifyCodeScreen;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterScreen extends AppCompatActivity {

    private TextView tvLogin, tvTerms;
    private EditText edt_userName, edtName, edtEmail, edtPhone, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private CheckBox checkBoxRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ UI
        edt_userName = findViewById(R.id.edt_userName);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        checkBoxRegister = findViewById(R.id.checkBoxRegister);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        tvTerms = findViewById(R.id.tvTerms);

        edtPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

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

        tvTerms.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterScreen.this, DieuKhoanvaDieuKien.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String userName = edt_userName.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (!validateInput(userName, name, email, phone, password, confirmPassword)) return;

        Map<String, String> emailBody = new HashMap<>();
        emailBody.put("email", email);

        ApiClient.getApiService().checkEmailExists(emailBody)
                .enqueue(new Callback<CheckEmailResponse>() {
                    @Override
                    public void onResponse(Call<CheckEmailResponse> call, Response<CheckEmailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            CheckEmailResponse checkEmail = response.body();
                            if (checkEmail.getMessage().equals("Email có thể sử dụng")) {
                                checkUsernameExists(userName, email, name, phone, password);
                            } else {
                                Toast.makeText(RegisterScreen.this, checkEmail.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterScreen.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckEmailResponse> call, Throwable t) {
                        Toast.makeText(RegisterScreen.this, "Lỗi mạng. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUsernameExists(String userName, String email, String name, String phone, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("username", userName);

        ApiClient.getApiService().checkUsernameExists(body)
                .enqueue(new Callback<CheckUsernameResponse>() {
                    @Override
                    public void onResponse(Call<CheckUsernameResponse> call, Response<CheckUsernameResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            CheckUsernameResponse checkUsername = response.body();
                            if (checkUsername.getMessage().equals("Username có thể sử dụng")) {
                                sendVerificationCode(email, userName, name, phone, password);
                            } else {
                                Toast.makeText(RegisterScreen.this, checkUsername.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterScreen.this, "Tên đăng nhập đã được sử dụng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckUsernameResponse> call, Throwable t) {
                        Toast.makeText(RegisterScreen.this, "Lỗi mạng. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendVerificationCode(String email, String username, String name, String phone, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("purpose", "register");

        ApiClient.getApiService().sendVerificationCode(body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Intent intent = new Intent(RegisterScreen.this, VerifyCodeScreen.class);
                            intent.putExtra("email", email);
                            intent.putExtra("purpose", "register");
                            intent.putExtra("name", name);
                            intent.putExtra("username", username);
                            intent.putExtra("phone", phone);
                            intent.putExtra("password", password);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RegisterScreen.this, "Gửi mã thất bại, thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(RegisterScreen.this, "Lỗi mạng. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInput(String userName, String name, String email, String phone, String password, String confirmPassword) {
        if (userName.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidPhone(phone)) {
            Toast.makeText(this, "Số điện thoại phải bắt đầu bằng 0 và có 10 chữ số", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidPassword(password)) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 1 chữ hoa, 1 số và 1 ký tự đặc biệt", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!checkBoxRegister.isChecked()) {
            Toast.makeText(this, "Bạn cần đồng ý điều khoản sử dụng", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleButton, boolean[] isVisible) {
        int selection = editText.getText().length();
        if (isVisible[0]) {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleButton.setImageResource(R.drawable.ic_eye_on);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleButton.setImageResource(R.drawable.ic_eye_off);
        }
        editText.setSelection(selection);
        isVisible[0] = !isVisible[0];
    }

    private boolean isValidEmail(String email) {
        return Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$").matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return Pattern.compile("^0\\d{9}$").matcher(phone).matches();
    }

    private boolean isValidPassword(String password) {
        return Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$").matcher(password).matches();
    }
}