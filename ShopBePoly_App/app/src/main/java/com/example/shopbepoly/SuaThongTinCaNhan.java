package com.example.shopbepoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.net.Uri;

public class SuaThongTinCaNhan extends AppCompatActivity {

    ImageButton btnBack;
    EditText editName, editBirthday, editPhone, editEmail;
    ImageView imageUpdateAvatar;
    Button btnSave;
    RadioGroup radioGroupGender;
    RadioButton radioMale, radioFemale;
    private User currentUser;
    private static final int REQUEST_CODE_PICK_IMAGE = 2001;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sua_thong_tin_ca_nhan);
        btnBack = findViewById(R.id.btnBack);
        editName = findViewById(R.id.editName);
        editBirthday = findViewById(R.id.editDob);
        editPhone = findViewById(R.id.editPhone);
        editEmail = findViewById(R.id.editEmail);
        imageUpdateAvatar = findViewById(R.id.imageUpdateAvatar);
        btnSave = findViewById(R.id.btnSave);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);

        btnBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent != null) {
            editName.setText(intent.getStringExtra("name") != null ? intent.getStringExtra("name") : "");
            editBirthday.setText(intent.getStringExtra("birthday") != null ? intent.getStringExtra("birthday") : "");
            editPhone.setText(intent.getStringExtra("phone") != null ? intent.getStringExtra("phone") : "");
            editEmail.setText(intent.getStringExtra("email") != null ? intent.getStringExtra("email") : "");
        }

        // Thêm chức năng chọn ngày sinh bằng DatePickerDialog
        editBirthday.setFocusable(false);
        editBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Lấy thông tin user hiện tại
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        loadCurrentUser(userId);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    updateUserInfo();
                }
            }
        });

        imageUpdateAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            }
        });
    }

    private void loadCurrentUser(String userId) {
        ApiService apiService = ApiClient.getApiService();
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (User user : response.body()) {
                        if (user.getId().equals(userId)) {
                            currentUser = user;
                            break;
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(SuaThongTinCaNhan.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Vui lòng nhập tên");
            return false;
        }

        if (email.isEmpty()) {
            editEmail.setError("Vui lòng nhập email");
            return false;
        }

        if (!email.contains("@")) {
            editEmail.setError("Email không hợp lệ");
            return false;
        }

        if (phone.isEmpty()) {
            editPhone.setError("Vui lòng nhập số điện thoại");
            return false;
        }

        if (!phone.matches("^\\d{8,10}$")) {
            editPhone.setError("Số điện thoại không hợp lệ");
            return false;
        }

        return true;
    }

    private void updateUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        
        if (userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = editName.getText().toString().trim();
        String birthday = editBirthday.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String gender = radioMale.isChecked() ? "Nam" : "Nữ";

        // Tạo user mới để cập nhật
        User updatedUser = new User();
        updatedUser.setId(userId);  // Set ID trước, không phụ thuộc vào currentUser

        // Cập nhật thông tin mới
        updatedUser.setName(name);
        updatedUser.setEmail(email);
        updatedUser.setPhone_number(phone);
        updatedUser.setGender(gender);
        updatedUser.setBirthday(birthday);
        
        // Giữ lại các thông tin không thay đổi từ currentUser nếu có
        if (currentUser != null) {
            updatedUser.setUsername(currentUser.getUsername());
            updatedUser.setPassword(currentUser.getPassword());
            updatedUser.setRole(currentUser.getRole());
            updatedUser.setAvatar(currentUser.getAvatar());
        } else {
            // Nếu không có currentUser, set các giá trị mặc định
            updatedUser.setUsername("");  // hoặc giá trị mặc định khác
            updatedUser.setPassword("");  // hoặc giữ nguyên password cũ từ SharedPreferences
            updatedUser.setRole(0);       // hoặc role mặc định
            updatedUser.setAvatar("");    // hoặc đường dẫn avatar mặc định
        }

        ApiService apiService = ApiClient.getApiService();
        android.util.Log.d("UserUpdate", "Đang gửi yêu cầu cập nhật cho userId: " + userId);
        android.util.Log.d("UserUpdate", "Dữ liệu gửi đi - Name: " + updatedUser.getName() + 
            ", Email: " + updatedUser.getEmail() + 
            ", Phone: " + updatedUser.getPhone_number() + 
            ", Birthday: " + updatedUser.getBirthday() +
            ", Gender: " + updatedUser.getGender());
            
        apiService.updateUser(userId, updatedUser).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                android.util.Log.d("UserUpdate", "Mã phản hồi: " + response.code());
                if (!response.isSuccessful() && response.errorBody() != null) {
                    try {
                        android.util.Log.e("UserUpdate", "Lỗi: " + response.errorBody().string());
                    } catch (Exception e) {
                        android.util.Log.e("UserUpdate", "Không thể đọc error body");
                    }
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    List<User> updatedUsers = response.body();
                    User updatedUser = null;
                    // Tìm user được cập nhật trong danh sách
                    for (User user : updatedUsers) {
                        if (user.getId().equals(userId)) {
                            updatedUser = user;
                            break;
                        }
                    }
                    
                    if (updatedUser != null) {
                        // Cập nhật SharedPreferences với thông tin mới
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("name", updatedUser.getName());
                        editor.putString("email", updatedUser.getEmail());
                        editor.putString("phone", updatedUser.getPhone_number());
                        editor.putString("birthday", updatedUser.getBirthday());
                        editor.putString("gender", updatedUser.getGender());
                        editor.apply();

                        android.util.Log.d("UserUpdate", "Đã lưu thông tin mới vào SharedPreferences");
                        Toast.makeText(SuaThongTinCaNhan.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(SuaThongTinCaNhan.this, "Không tìm thấy thông tin người dùng sau khi cập nhật", Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMessage;
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        } else {
                            errorMessage = "Lỗi không xác định: " + response.code();
                        }
                    } catch (Exception e) {
                        errorMessage = "Lỗi không xác định: " + response.code();
                    }
                    Toast.makeText(SuaThongTinCaNhan.this, "Cập nhật thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                android.util.Log.e("UserUpdate", "Lỗi kết nối: " + t.getMessage(), t);
                Toast.makeText(SuaThongTinCaNhan.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Thêm hàm showDatePickerDialog vào cuối file
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        String birthdayText = editBirthday.getText().toString();
        
        // Thử parse ngày sinh hiện tại nếu có
        if (!birthdayText.isEmpty()) {
            try {
                // Thử parse cả 2 định dạng
                SimpleDateFormat sdfOld = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat sdfNew = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                try {
                    calendar.setTime(sdfOld.parse(birthdayText));
                } catch (Exception e) {
                    try {
                        calendar.setTime(sdfNew.parse(birthdayText));
                    } catch (Exception e2) {
                        // Nếu không parse được thì dùng ngày hiện tại
                    }
                }
            } catch (Exception e) {
                // Nếu lỗi thì giữ ngày hiện tại
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, month1, dayOfMonth);
            
            // Format theo định dạng yyyy-MM-dd cho backend
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(selectedDate.getTime());
            editBirthday.setText(formattedDate);
        }, year, month, day);

        // Set giới hạn ngày chọn (tuỳ chọn)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -10); // Ít nhất 10 tuổi
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageUpdateAvatar.setImageURI(selectedImageUri); // Hiển thị ảnh vừa chọn lên ImageView
            // Nếu muốn upload lên server, xử lý thêm ở đây
        }

    }
}