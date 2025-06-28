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

public class AddEditAddressActivity extends AppCompatActivity {
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
    private boolean isLoadingData = false; // Flag để tránh trigger listener khi đang load dữ liệu
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

        // Nếu là sửa, nhận dữ liệu và hiển thị lên các ô
        String editJson = getIntent().getStringExtra("edit_address");
        if (editJson != null) {
            editingAddress = gson.fromJson(editJson, Address.class);
            loadEditData();
        } else {
            // Nếu là thêm mới, load địa chỉ mặc định hiện tại
            loadDefaultAddressData();
        }
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

        // Khởi tạo các list để tránh NullPointerException
        districts = new java.util.ArrayList<>();
        wards = new java.util.ArrayList<>();
    }

    private void setupSpinners() {
        String[] labels = {"Nhà", "Công ty", "Khác"};
        ArrayAdapter<String> labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
        spinnerLabel.setAdapter(labelAdapter);
    }

    private void loadProvinces() {
        provinces = VietnamAddress.getProvinces();
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
        // Spinner tỉnh
        spinnerProvince.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (isLoadingData) return; // Bỏ qua nếu đang load dữ liệu
                
                VietnamAddress selectedProvince = provinces.get(position);
                selectedProvinceCode = selectedProvince.getCode();
                loadDistricts(selectedProvinceCode);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                if (isLoadingData) return; // Bỏ qua nếu đang load dữ liệu
                
                selectedProvinceCode = "";
                districts.clear();
                wards.clear();
                updateDistrictSpinner();
                updateWardSpinner();
            }
        });

        // Spinner huyện
        spinnerDistrict.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (isLoadingData) return; // Bỏ qua nếu đang load dữ liệu
                
                if (position >= 0 && position < districts.size()) {
                    VietnamAddress.District selectedDistrict = districts.get(position);
                    selectedDistrictCode = selectedDistrict.getCode();
                    loadWards(selectedDistrictCode);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                if (isLoadingData) return; // Bỏ qua nếu đang load dữ liệu
                
                selectedDistrictCode = "";
                wards.clear();
                updateWardSpinner();
            }
        });

        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void loadDistricts(String provinceCode) {
        districts = VietnamAddress.getDistrictsByProvince(provinceCode);
        updateDistrictSpinner();
        // Reset ward spinner
        if (!isLoadingData) {
            wards.clear();
            updateWardSpinner();
        }
    }

    private void loadWards(String districtCode) {
        wards = VietnamAddress.getWardsByDistrict(districtCode);
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
        isLoadingData = true; // Bắt đầu load dữ liệu
        
        edtName.setText(editingAddress.getName());
        edtPhone.setText(editingAddress.getPhone());
        
        // Để trống ô địa chỉ chi tiết để người dùng nhập mới
        edtAddress.setText("");
        
        // Set label
        String[] labels = {"Nhà", "Công ty", "Khác"};
        int labelIndex = 0;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equalsIgnoreCase(editingAddress.getLabel())) {
                labelIndex = i; 
                break;
            }
        }
        spinnerLabel.setSelection(labelIndex);
        checkboxDefault.setChecked(editingAddress.isDefault());
        
        // Set các spinner tỉnh/huyện/xã từ địa chỉ đang edit
        setAddressSpinnersFromFullAddress(editingAddress.getAddress());
        
        isLoadingData = false; // Kết thúc load dữ liệu
    }

    private void loadDefaultAddressData() {
        isLoadingData = true; // Bắt đầu load dữ liệu
        
        // Load địa chỉ mặc định từ SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("AddressPrefs", MODE_PRIVATE);
        String addressJson = prefs.getString("default_address_" + userId, "");
        if (!addressJson.isEmpty()) {
            try {
                Address defaultAddress = gson.fromJson(addressJson, Address.class);
                if (defaultAddress != null) {
                    // Cập nhật thông tin cá nhân vào form
                    edtName.setText(defaultAddress.getName());
                    edtPhone.setText(defaultAddress.getPhone());
                    
                    // Để trống ô địa chỉ chi tiết để người dùng nhập mới
                    edtAddress.setText("");
                    
                    // Set label
                    String[] labels = {"Nhà", "Công ty", "Khác"};
                    int labelIndex = 0;
                    for (int i = 0; i < labels.length; i++) {
                        if (labels[i].equalsIgnoreCase(defaultAddress.getLabel())) {
                            labelIndex = i; 
                            break;
                        }
                    }
                    spinnerLabel.setSelection(labelIndex);
                    
                    // Set các spinner tỉnh/huyện/xã từ địa chỉ mặc định
                    setAddressSpinnersFromFullAddress(defaultAddress.getAddress());
                    
                    // Không set checkbox mặc định vì đây là địa chỉ mới
                    checkboxDefault.setChecked(false);
                    
                    android.util.Log.d("AddEditAddress", "Loaded default address info: " + defaultAddress.getName());
                }
            } catch (Exception e) {
                android.util.Log.e("AddEditAddress", "Error parsing default address", e);
            }
        }
        
        isLoadingData = false; // Kết thúc load dữ liệu
    }

    private void setAddressSpinnersFromFullAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.isEmpty()) return;
        
        // Tách địa chỉ thành các phần: "123 Nguyễn Huệ, Phường 1, Quận 1, TP. Hồ Chí Minh"
        String[] parts = fullAddress.split(",");
        if (parts.length >= 4) {
            String wardName = parts[1].trim();
            String districtName = parts[2].trim();
            String provinceName = parts[3].trim();
            
            // Tìm và set tỉnh
            for (int i = 0; i < provinces.size(); i++) {
                if (provinces.get(i).getName().equalsIgnoreCase(provinceName)) {
                    spinnerProvince.setSelection(i);
                    selectedProvinceCode = provinces.get(i).getCode();
                    
                    // Load districts cho tỉnh này (không trigger listener vì isLoadingData = true)
                    districts = VietnamAddress.getDistrictsByProvince(selectedProvinceCode);
                    updateDistrictSpinner();
                    
                    // Tìm và set huyện
                    for (int j = 0; j < districts.size(); j++) {
                        if (districts.get(j).getName().equalsIgnoreCase(districtName)) {
                            spinnerDistrict.setSelection(j);
                            selectedDistrictCode = districts.get(j).getCode();
                            
                            // Load wards cho huyện này (không trigger listener vì isLoadingData = true)
                            wards = VietnamAddress.getWardsByDistrict(selectedDistrictCode);
                            updateWardSpinner();
                            
                            // Tìm và set xã
                            for (int k = 0; k < wards.size(); k++) {
                                if (wards.get(k).getName().equalsIgnoreCase(wardName)) {
                                    spinnerWard.setSelection(k);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    private void saveAddress() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String addressDetail = edtAddress.getText().toString().trim();
        String label = spinnerLabel.getSelectedItem().toString();
        boolean isDefault = checkboxDefault.isChecked();

        if (name.isEmpty() || phone.isEmpty() || addressDetail.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo địa chỉ đầy đủ từ các thành phần
        String fullAddress = buildFullAddress(addressDetail);
        
        Address result;
        if (editingAddress != null) {
            result = new Address(editingAddress.getId(), name, phone, fullAddress, label, isDefault);
        } else {
            result = new Address(UUID.randomUUID().toString(), name, phone, fullAddress, label, isDefault);
        }
        
        Intent intent = new Intent();
        intent.putExtra("address_result", gson.toJson(result));
        setResult(RESULT_OK, intent);
        finish();
    }

    private String buildFullAddress(String addressDetail) {
        StringBuilder fullAddress = new StringBuilder();
        
        if (!addressDetail.isEmpty()) {
            fullAddress.append(addressDetail).append(", ");
        }
        
        if (spinnerWard.getSelectedItem() != null) {
            VietnamAddress.Ward selectedWard = (VietnamAddress.Ward) spinnerWard.getSelectedItem();
            fullAddress.append(selectedWard.getName()).append(", ");
        }
        
        if (spinnerDistrict.getSelectedItem() != null) {
            VietnamAddress.District selectedDistrict = (VietnamAddress.District) spinnerDistrict.getSelectedItem();
            fullAddress.append(selectedDistrict.getName()).append(", ");
        }
        
        if (spinnerProvince.getSelectedItem() != null) {
            VietnamAddress selectedProvince = (VietnamAddress) spinnerProvince.getSelectedItem();
            fullAddress.append(selectedProvince.getName());
        }
        
        return fullAddress.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_MAP && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("selected_address");
            if (address != null) edtAddress.setText(address);
        }
    }
}
