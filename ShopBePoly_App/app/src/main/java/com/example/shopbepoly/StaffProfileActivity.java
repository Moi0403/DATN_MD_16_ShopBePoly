package com.example.shopbepoly;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.User;
import com.example.shopbepoly.Screen.LoginScreen;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffProfileActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private TextView txtUsername;
    private ImageButton btnBack;
    private LinearLayout layoutPersonalInfo;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutLogout;
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_profile);

        imgAvatar = findViewById(R.id.imgAvatar);
        txtUsername = findViewById(R.id.txtUsername);
        btnBack = findViewById(R.id.btnBack);
        layoutPersonalInfo = findViewById(R.id.layoutPersonalInfo);
        layoutChangePassword = findViewById(R.id.layoutChangePassword);
        layoutLogout = findViewById(R.id.layoutLogout);

        // Xử lý sự kiện nút back
        btnBack.setOnClickListener(v -> finish());

        // Xử lý sự kiện thông tin cá nhân
        layoutPersonalInfo.setOnClickListener(v -> showPersonalInfoDialog());

        // Xử lý sự kiện đổi mật khẩu
        layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Xử lý sự kiện đăng xuất
        layoutLogout.setOnClickListener(v -> showLogoutConfirmDialog());

        loadStaffInfo();
    }

    private void showPersonalInfoDialog() {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_personal_info, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Tìm các view trong dialog
        ImageView dialogImgAvatar = dialogView.findViewById(R.id.dialogImgAvatar);
        TextView dialogTxtUsername = dialogView.findViewById(R.id.dialogTxtUsername);
        TextView dialogTxtPhone = dialogView.findViewById(R.id.dialogTxtPhone);
        TextView dialogTxtEmail = dialogView.findViewById(R.id.dialogTxtEmail);
        Button btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);

        // Hiển thị thông tin
        dialogTxtUsername.setText(currentUser.getName() != null ? currentUser.getName() : "");
        dialogTxtPhone.setText(currentUser.getPhone_number() != null ? currentUser.getPhone_number() : "");
        dialogTxtEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");

        // Load avatar
        String avatarFileName = currentUser.getAvatar();
        if (avatarFileName != null && !avatarFileName.isEmpty()) {
            String fullUrl = ApiClient.IMAGE_URL + avatarFileName + "?t=" + System.currentTimeMillis();
            Glide.with(this)
                    .load(fullUrl)
                    .apply(new RequestOptions()
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.ic_avatar)
                            .error(R.drawable.ic_avatar))
                    .into(dialogImgAvatar);
        } else {
            dialogImgAvatar.setImageResource(R.drawable.ic_avatar);
        }

        // Xử lý nút đóng
        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void loadStaffInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        ApiService apiService = ApiClient.getApiService();
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (User user : response.body()) {
                        if (user.getId().equals(userId)) {
                            currentUser = user;
                            txtUsername.setText(user.getName() != null ? user.getName() : "");
                            String avatarFileName = user.getAvatar();
                            if (avatarFileName != null && !avatarFileName.isEmpty()) {
                                String fullUrl = ApiClient.IMAGE_URL + avatarFileName + "?t=" + System.currentTimeMillis();
                                Glide.with(StaffProfileActivity.this)
                                        .load(fullUrl)
                                        .apply(new RequestOptions()
                                                .transform(new CircleCrop())
                                                .placeholder(R.drawable.ic_avatar)
                                                .error(R.drawable.ic_avatar))
                                        .into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(R.drawable.ic_avatar);
                            }
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Tìm các view trong dialog
        EditText edtCurrentPassword = dialogView.findViewById(R.id.edtCurrentPassword);
        EditText edtNewPassword = dialogView.findViewById(R.id.edtNewPassword);
        EditText edtConfirmPassword = dialogView.findViewById(R.id.edtConfirmPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnChangePassword = dialogView.findViewById(R.id.btnChangePassword);

        // Xử lý nút hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Xử lý nút đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = edtCurrentPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            // Validation
            if (currentPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra mật khẩu hiện tại
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String savedPassword = sharedPreferences.getString("password", "");
            
            if (!currentPassword.equals(savedPassword)) {
                Toast.makeText(this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thực hiện đổi mật khẩu
            changePassword(newPassword, dialog);
        });

        dialog.show();
    }

    private void changePassword(String newPassword, AlertDialog dialog) {
        if (currentUser == null) {
            Toast.makeText(this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo object User với mật khẩu mới
        User updatedUser = new User();
        updatedUser.setId(currentUser.getId());
        updatedUser.setUsername(currentUser.getUsername());
        updatedUser.setEmail(currentUser.getEmail());
        updatedUser.setPhone_number(currentUser.getPhone_number());
        updatedUser.setName(currentUser.getName());
        updatedUser.setAddress(currentUser.getAddress());
        updatedUser.setRole(currentUser.getRole());
        updatedUser.setAvatar(currentUser.getAvatar());
        updatedUser.setGender(currentUser.getGender());
        updatedUser.setBirthday(currentUser.getBirthday());
        updatedUser.setPassword(newPassword);

        ApiService apiService = ApiClient.getApiService();
        apiService.updateUser(currentUser.getId(), updatedUser).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    // Cập nhật mật khẩu trong SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("password", newPassword);
                    editor.apply();

                    Toast.makeText(StaffProfileActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(StaffProfileActivity.this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(StaffProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_logout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Tìm các view trong dialog
        Button btnCancelLogout = dialogView.findViewById(R.id.btnNo);
        Button btnConfirmLogout = dialogView.findViewById(R.id.btnYes);

        // Xử lý nút hủy
        btnCancelLogout.setOnClickListener(v -> dialog.dismiss());

        // Xử lý nút xác nhận đăng xuất
        btnConfirmLogout.setOnClickListener(v -> {
            dialog.dismiss();
            performLogout();
        });

        dialog.show();
    }

    private void performLogout() {
        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        boolean isRemembered = preferences.getBoolean("remember", false);
        String savedUsername = preferences.getString("username", "");
        String savedPassword = preferences.getString("password", "");

        editor.clear();
        if (isRemembered) {
            editor.putString("username", savedUsername);
            editor.putString("password", savedPassword);
            editor.putBoolean("remember", true);
        }
        editor.apply();
        
        Intent intent = new Intent(this, LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}


