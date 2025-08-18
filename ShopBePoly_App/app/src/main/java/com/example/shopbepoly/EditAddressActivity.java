package com.example.shopbepoly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;
import com.example.shopbepoly.DTO.Address;
import com.example.shopbepoly.DTO.VietnamAddress;
import com.google.gson.Gson;
import java.util.List;
import java.util.ArrayList;
import android.content.SharedPreferences;
import com.example.shopbepoly.DTO.User;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditAddressActivity extends AppCompatActivity {
    private EditText edtName, edtPhone, edtAddress;
    private Spinner spinnerLabel, spinnerProvince, spinnerDistrict, spinnerWard;
    private CheckBox checkboxDefault;
    private Button btnSave;
    private Button btnPickOnMap;
    private Address editingAddress = null;
    private Gson gson = new Gson();
    private static final int REQ_MAP = 2001;

    private List<VietnamAddress> provinces;
    private List<VietnamAddress.District> districts;
    private List<VietnamAddress.Ward> wards;
    private String selectedProvinceCode = "";
    private String selectedDistrictCode = "";
    private boolean isLoadingData = false;
    private String userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_address);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy userId
        android.content.SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        userId = loginPrefs.getString("userId", "");

        // Lấy thông tin user từ API
        loadUserInfo();

        initViews();
        setupSpinners();
        loadProvinces();
        setupListeners();
        setTitle("Sửa Địa Chỉ");
        android.widget.TextView txtTitle = findViewById(R.id.txtTitle);
        if (txtTitle != null) txtTitle.setText("Sửa Địa Chỉ");

        // Nhận dữ liệu địa chỉ cần sửa
        String editJson = getIntent().getStringExtra("edit_address");
        if (editJson != null) {
            editingAddress = gson.fromJson(editJson, Address.class);
            loadEditData();
        }
    }

    private void loadUserInfo() {
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
                // Có thể xử lý lỗi nếu cần
            }
        });
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        spinnerLabel = findViewById(R.id.spinnerLabel);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerWard = findViewById(R.id.spinnerWard);
        checkboxDefault = findViewById(R.id.checkboxDefault);
        btnSave = findViewById(R.id.btnSave);
        districts = new ArrayList<>();
        wards = new ArrayList<>();
    }

    private void setupSpinners() {
        String[] labels = {"Nhà", "Công ty", "Khác"};
        ArrayAdapter<String> labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
        spinnerLabel.setAdapter(labelAdapter);
    }

    private void loadProvinces() {
        provinces = VietnamAddress.loadFromJson(this);
        ArrayAdapter<VietnamAddress> provinceAdapter = new ArrayAdapter<VietnamAddress>(this, android.R.layout.simple_spinner_dropdown_item, provinces) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.widget.TextView textView = (android.widget.TextView) super.getView(position, convertView, parent);
                VietnamAddress province = getItem(position);
                if (province != null) {
                    textView.setText(province.getName());
                }
                return textView;
            }
            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.widget.TextView textView = (android.widget.TextView) super.getDropDownView(position, convertView, parent);
                VietnamAddress province = getItem(position);
                if (province != null) {
                    textView.setText(province.getName());
                }
                return textView;
            }
        };
        spinnerProvince.setAdapter(provinceAdapter);
    }

    private void setupListeners() {
        spinnerProvince.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                VietnamAddress selectedProvince = provinces.get(position);
                selectedProvinceCode = selectedProvince.getCode();
                loadDistricts(selectedProvinceCode);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedProvinceCode = "";
                districts.clear();
                wards.clear();
                updateDistrictSpinner();
                updateWardSpinner();
            }
        });
        spinnerDistrict.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position >= 0 && position < districts.size()) {
                    VietnamAddress.District selectedDistrict = districts.get(position);
                    selectedDistrictCode = selectedDistrict.getCode();
                    loadWards(selectedDistrictCode);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedDistrictCode = "";
                wards.clear();
                updateWardSpinner();
            }
        });
        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void loadDistricts(String provinceCode) {
        districts = VietnamAddress.getDistrictsByProvince(provinces, provinceCode);
        updateDistrictSpinner();
        wards = new ArrayList<>();
        updateWardSpinner();
    }

    private void loadWards(String districtCode) {
        wards = VietnamAddress.getWardsByDistrict(provinces, districtCode);
        updateWardSpinner();
    }

    private void updateDistrictSpinner() {
        ArrayAdapter<VietnamAddress.District> districtAdapter = new ArrayAdapter<VietnamAddress.District>(this, android.R.layout.simple_spinner_dropdown_item, districts) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.widget.TextView textView = (android.widget.TextView) super.getView(position, convertView, parent);
                VietnamAddress.District district = getItem(position);
                if (district != null) {
                    textView.setText(district.getName());
                }
                return textView;
            }
            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.widget.TextView textView = (android.widget.TextView) super.getDropDownView(position, convertView, parent);
                VietnamAddress.District district = getItem(position);
                if (district != null) {
                    textView.setText(district.getName());
                }
                return textView;
            }
        };
        spinnerDistrict.setAdapter(districtAdapter);
    }

    private void updateWardSpinner() {
        ArrayAdapter<VietnamAddress.Ward> wardAdapter = new ArrayAdapter<VietnamAddress.Ward>(this, android.R.layout.simple_spinner_dropdown_item, wards) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.widget.TextView textView = (android.widget.TextView) super.getView(position, convertView, parent);
                VietnamAddress.Ward ward = getItem(position);
                if (ward != null) {
                    textView.setText(ward.getName());
                }
                return textView;
            }
            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.widget.TextView textView = (android.widget.TextView) super.getDropDownView(position, convertView, parent);
                VietnamAddress.Ward ward = getItem(position);
                if (ward != null) {
                    textView.setText(ward.getName());
                }
                return textView;
            }
        };
        spinnerWard.setAdapter(wardAdapter);
    }

    private void loadEditData() {
        if (editingAddress == null) return;
        edtName.setText(editingAddress.getName());
        edtPhone.setText(editingAddress.getPhone());
        edtAddress.setText(editingAddress.getAddress().split(",")[0]);
        checkboxDefault.setChecked(editingAddress.isDefault());
        // Set label
        String[] labels = {"Nhà", "Công ty", "Khác"};
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(editingAddress.getLabel())) {
                spinnerLabel.setSelection(i);
                break;
            }
        }
        // Set các spinner địa chỉ (province, district, ward)
        setAddressSpinnersFromFullAddress(editingAddress.getAddress());
    }

    private String normalizeAdminName(String name) {
        if (name == null) return "";
        return name.replace("Tỉnh ", "").replace("Thành phố ", "").replace("Quận ", "").replace("Huyện ", "").replace("Thị xã ", "").replace("Phường ", "").replace("Xã ", "").replace("Thị trấn ", "");
    }

    private void setAddressSpinnersFromFullAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.isEmpty()) return;
        String[] parts = fullAddress.split(",");
        if (parts.length >= 3) {
            String provinceName = parts[parts.length - 1].trim();
            String districtName = parts[parts.length - 2].trim();
            String wardName = parts[parts.length - 3].trim();

            isLoadingData = true;

            // 1. Set tỉnh
            int provinceIndex = -1;
            String normProvince = normalizeAdminName(provinceName);
            for (int i = 0; i < provinces.size(); i++) {
                if (normalizeAdminName(provinces.get(i).getName()).equals(normProvince)) {
                    provinceIndex = i;
                    break;
                }
            }
            if (provinceIndex != -1) {
                spinnerProvince.setSelection(provinceIndex, false);
                selectedProvinceCode = provinces.get(provinceIndex).getCode();
            }

            // 2. Load danh sách huyện từ tỉnh đã chọn
            districts = VietnamAddress.getDistrictsByProvince(provinces, selectedProvinceCode);
            updateDistrictSpinner();

            // Tìm index của huyện
            int districtIndex = -1;
            String normDistrict = normalizeAdminName(districtName);
            for (int i = 0; i < districts.size(); i++) {
                if (normalizeAdminName(districts.get(i).getName()).equals(normDistrict)) {
                    districtIndex = i;
                    break;
                }
            }

            final int finalDistrictIndex = districtIndex;
            if (districtIndex != -1) {
                // Delay setSelection sau khi adapter hoàn tất
                spinnerDistrict.post(() -> {
                    spinnerDistrict.setSelection(finalDistrictIndex, false);
                    selectedDistrictCode = districts.get(finalDistrictIndex).getCode();

                    // 3. Load danh sách xã từ huyện đã chọn
                    wards = VietnamAddress.getWardsByDistrict(provinces, selectedDistrictCode);
                    updateWardSpinner();

                    // Tìm index xã
                    int wardIndex = -1;
                    String normWard = normalizeAdminName(wardName);
                    for (int i = 0; i < wards.size(); i++) {
                        if (normalizeAdminName(wards.get(i).getName()).equals(normWard)) {
                            wardIndex = i;
                            break;
                        }
                    }

                    final int finalWardIndex = wardIndex;
                    if (wardIndex != -1) {
                        spinnerWard.post(() -> {
                            spinnerWard.setSelection(finalWardIndex, false);
                            isLoadingData = false;
                        });
                    } else {
                        isLoadingData = false;
                    }
                });
            } else {
                isLoadingData = false;
            }
        }
    }

    private String buildFullAddress(String addressDetail) {
        String province = spinnerProvince.getSelectedItem() != null ? ((VietnamAddress) spinnerProvince.getSelectedItem()).getName() : "";
        String district = spinnerDistrict.getSelectedItem() != null ? ((VietnamAddress.District) spinnerDistrict.getSelectedItem()).getName() : "";
        String ward = spinnerWard.getSelectedItem() != null ? ((VietnamAddress.Ward) spinnerWard.getSelectedItem()).getName() : "";
        return addressDetail + ", " + ward + ", " + district + ", " + province;
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.length() == 10 && phone.matches("\\d+") && phone.startsWith("0");
    }

    private void saveAddress() {
        if (editingAddress == null) return;
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String addressDetail = edtAddress.getText().toString().trim();
        String label = spinnerLabel.getSelectedItem().toString();
        boolean isDefault = checkboxDefault.isChecked();
        String fullAddress = buildFullAddress(addressDetail);

        // Kiểm tra thông tin bắt buộc
        if (name.isEmpty() || phone.isEmpty() || addressDetail.isEmpty() || spinnerProvince.getSelectedItem() == null || spinnerDistrict.getSelectedItem() == null || spinnerWard.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra số điện thoại phải đủ 10 số và bắt đầu bằng 0
        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(this, "Số điện thoại phải có đúng 10 chữ số và bắt đầu bằng số 0!", Toast.LENGTH_SHORT).show();
            return;
        }
        editingAddress.setName(name);
        editingAddress.setPhone(phone);
        editingAddress.setAddress(fullAddress);
        editingAddress.setLabel(label);
        editingAddress.setDefault(isDefault);

        // Cập nhật thông tin user nếu là địa chỉ mặc định
        if (isDefault && currentUser != null) {
            currentUser.setName(name);
            currentUser.setPhone_number(phone);
            String province = spinnerProvince.getSelectedItem() != null ? ((VietnamAddress) spinnerProvince.getSelectedItem()).getName() : "";
            String district = spinnerDistrict.getSelectedItem() != null ? ((VietnamAddress.District) spinnerDistrict.getSelectedItem()).getName() : "";
            String ward = spinnerWard.getSelectedItem() != null ? ((VietnamAddress.Ward) spinnerWard.getSelectedItem()).getName() : "";
            String fullUserAddress = addressDetail + ", " + ward + ", " + district + ", " + province;
            currentUser.setAddress(fullUserAddress);
            // Gọi API cập nhật user
            ApiService apiService = ApiClient.getApiService();
            apiService.updateUser(userId, currentUser).enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                    // Thành công, có thể thông báo hoặc reload UI nếu cần
                }
                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {
                    // Thông báo lỗi nếu cần
                }
            });
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("address_result", gson.toJson(editingAddress));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
} 