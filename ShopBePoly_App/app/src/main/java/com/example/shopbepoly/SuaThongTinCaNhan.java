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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.FileUtil;
import com.example.shopbepoly.DTO.User;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
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
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
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

                            // ✅ Hiển thị avatar nếu có
                            if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                                Glide.with(SuaThongTinCaNhan.this)
                                        .load(user.getAvatar())
                                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                        .placeholder(R.drawable.ic_avatar)
                                        .error(R.drawable.ic_avatar)
                                        .into(imageUpdateAvatar);
                            }

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

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(name);
        updatedUser.setEmail(email);
        updatedUser.setPhone_number(phone);
        updatedUser.setGender(gender);
        updatedUser.setBirthday(birthday);

        if (currentUser != null) {
            updatedUser.setUsername(currentUser.getUsername());
            updatedUser.setPassword(currentUser.getPassword());
            updatedUser.setRole(currentUser.getRole());
            updatedUser.setAvatar(currentUser.getAvatar());
        } else {
            updatedUser.setUsername("");
            updatedUser.setPassword("");
            updatedUser.setRole(0);
            updatedUser.setAvatar("");
        }

        // 🔽 Thêm đoạn này
        if (selectedImageUri != null) {
            uploadAvatarAndUpdateInfo(userId, updatedUser);
        } else {
            updateUserWithoutAvatar(userId, updatedUser);
        }
    }
    private void uploadAvatarAndUpdateInfo(String userId, User updatedUser) {
        try {
            File file = FileUtil.from(this, selectedImageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("avt_user", file.getName(), requestFile);

            ApiService apiService = ApiClient.getApiService();
            apiService.uploadAvatar(userId, body).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String newAvatarUrl = response.body().getAvatar();
                        updatedUser.setAvatar(newAvatarUrl);

// Cập nhật lại hình ảnh trên giao diện bằng Glide
                        Glide.with(SuaThongTinCaNhan.this)
                                .load(currentUser.getAvatar()) // ✅ đúng là avatar từ server
                                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                .placeholder(R.drawable.ic_avatar)
                                .error(R.drawable.ic_avatar)
                                .into(imageUpdateAvatar);


                        updateUserWithoutAvatar(userId, updatedUser);
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Không có nội dung lỗi";
                            Toast.makeText(SuaThongTinCaNhan.this, "Lỗi upload ảnh: " + errorBody, Toast.LENGTH_LONG).show();
                            System.err.println("Upload ảnh lỗi - Code: " + response.code() + ", body: " + errorBody);
                        } catch (Exception e) {
                            Toast.makeText(SuaThongTinCaNhan.this, "Lỗi upload ảnh", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(SuaThongTinCaNhan.this, "Lỗi kết nối khi upload ảnh", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateUserWithoutAvatar(String userId, User updatedUser) {
        ApiService apiService = ApiClient.getApiService();
        apiService.updateUser(userId, updatedUser).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SuaThongTinCaNhan.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();


                } else {
                    Toast.makeText(SuaThongTinCaNhan.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(SuaThongTinCaNhan.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
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

            // Giữ quyền truy cập dài hạn với ảnh SAF
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);

            // ✅ Dùng Glide để hiển thị ảnh bo tròn
            Glide.with(this)
                    .load(selectedImageUri)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(imageUpdateAvatar);
        }
    }

}