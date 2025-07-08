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
import java.util.UUID;
import java.util.ArrayList;

public class AddAddressActivity extends AppCompatActivity {
    private EditText edtName, edtPhone, edtAddress;
    private Spinner spinnerLabel, spinnerProvince, spinnerDistrict, spinnerWard;
    private CheckBox checkboxDefault;
    private Button btnSave;
    private Button btnPickOnMap;
    private Gson gson = new Gson();
    private static final int REQ_MAP = 2001;

    private List<VietnamAddress> provinces;
    private List<VietnamAddress.District> districts;
    private List<VietnamAddress.Ward> wards;
    private String selectedProvinceCode = "";
    private String selectedDistrictCode = "";
    private boolean isLoadingData = false;
    private String userId;

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

        initViews();
        setupSpinners();
        loadProvinces();
        setupListeners();
        loadDefaultAddressData();
        setTitle("Thêm Địa Chỉ");
        android.widget.TextView txtTitle = findViewById(R.id.txtTitle);
        if (txtTitle != null) txtTitle.setText("Thêm Địa Chỉ");
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

    private void loadDefaultAddressData() {
        // Có thể lấy địa chỉ mặc định từ SharedPreferences nếu muốn tự động điền
        // Ở đây để trống cho người dùng nhập mới
    }

    private String normalizeAdminName(String name) {
        if (name == null) return "";
        return name.replace("Tỉnh ", "").replace("Thành phố ", "").replace("Quận ", "").replace("Huyện ", "").replace("Thị xã ", "").replace("Phường ", "").replace("Xã ", "").replace("Thị trấn ", "");
    }

    private void setAddressSpinnersFromFullAddress(String fullAddress) {
        // Hàm này có thể dùng để parse lại địa chỉ đầy đủ và set lại các spinner nếu cần
        // Để trống vì màn thêm không cần
    }

    private String buildFullAddress(String addressDetail) {
        String province = spinnerProvince.getSelectedItem() != null ? ((VietnamAddress) spinnerProvince.getSelectedItem()).getName() : "";
        String district = spinnerDistrict.getSelectedItem() != null ? ((VietnamAddress.District) spinnerDistrict.getSelectedItem()).getName() : "";
        String ward = spinnerWard.getSelectedItem() != null ? ((VietnamAddress.Ward) spinnerWard.getSelectedItem()).getName() : "";
        return addressDetail + ", " + ward + ", " + district + ", " + province;
    }

    private void saveAddress() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String addressDetail = edtAddress.getText().toString().trim();
        String label = spinnerLabel.getSelectedItem().toString();
        boolean isDefault = checkboxDefault.isChecked();
        String fullAddress = buildFullAddress(addressDetail);
        if (name.isEmpty() || phone.isEmpty() || addressDetail.isEmpty() || spinnerProvince.getSelectedItem() == null || spinnerDistrict.getSelectedItem() == null || spinnerWard.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        Address newAddress = new Address();
        newAddress.setId(UUID.randomUUID().toString());
        newAddress.setName(name);
        newAddress.setPhone(phone);
        newAddress.setAddress(fullAddress);
        newAddress.setLabel(label);
        newAddress.setDefault(isDefault);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("address_result", gson.toJson(newAddress));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
} 