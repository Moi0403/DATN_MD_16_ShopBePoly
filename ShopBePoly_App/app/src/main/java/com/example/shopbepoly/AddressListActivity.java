package com.example.shopbepoly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shopbepoly.Adapter.AddressAdapter;
import com.example.shopbepoly.DTO.Address;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressListActivity extends AppCompatActivity implements AddressAdapter.AddressListener {
    private static final int REQ_ADD = 1001;
    private static final int REQ_EDIT = 1002;
    private RecyclerView recyclerView;
    private AddressAdapter adapter;
    private List<Address> addressList;
    private SharedPreferences prefs;
    private Gson gson = new Gson();
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);
        recyclerView = findViewById(R.id.recyclerAddress);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        prefs = getSharedPreferences("AddressPrefs", Context.MODE_PRIVATE);
        SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        userId = loginPrefs.getString("userId", "");
        addressList = loadAddresses();
        adapter = new AddressAdapter(this, addressList, this);
        recyclerView.setAdapter(adapter);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditAddressActivity.class);
            startActivityForResult(intent, REQ_ADD);
        });
    }

    private List<Address> loadAddresses() {
        String json = prefs.getString("address_list_" + userId, "");
        if (json.isEmpty()) return new ArrayList<>();
        Type type = new com.google.gson.reflect.TypeToken<List<Address>>(){}.getType();
        List<Address> list = gson.fromJson(json, type);
        // Đảm bảo luôn có 1 địa chỉ mặc định
        if (!list.isEmpty() && list.stream().noneMatch(Address::isDefault)) {
            list.get(0).setDefault(true);
            // Lưu lại nếu phải sửa
            prefs.edit().putString("address_list_" + userId, gson.toJson(list)).apply();
            prefs.edit().putString("default_address_" + userId, gson.toJson(list.get(0))).apply();
        }
        return list;
    }

    private void saveAddresses() {
        prefs.edit().putString("address_list_" + userId, gson.toJson(addressList)).apply();
    }

    @Override
    public void onEdit(Address address) {
        Intent intent = new Intent(this, AddEditAddressActivity.class);
        intent.putExtra("edit_address", gson.toJson(address));
        startActivityForResult(intent, REQ_EDIT);
    }

    @Override
    public void onDelete(Address address) {
        if (address.isDefault()) {
            Toast.makeText(this, "Không thể xóa địa chỉ mặc định! Hãy chọn địa chỉ khác làm mặc định trước.", Toast.LENGTH_SHORT).show();
            return;
        }
        addressList.remove(address);
        // Nếu sau khi xóa chỉ còn 1 địa chỉ, set nó là mặc định
        if (addressList.size() == 1) {
            addressList.get(0).setDefault(true);
        } else if (!addressList.isEmpty() && addressList.stream().noneMatch(Address::isDefault)) {
            addressList.get(0).setDefault(true);
        }
        saveAddresses();
        // Luôn cập nhật lại địa chỉ mặc định hiện tại vào SharedPreferences (nếu còn địa chỉ)
        if (!addressList.isEmpty()) {
            Address currentDefault = addressList.stream().filter(Address::isDefault).findFirst().orElse(addressList.get(0));
            android.util.Log.d("AddressListActivity", "Cập nhật default_address: " + currentDefault.getName() + " - " + currentDefault.getPhone() + " - " + currentDefault.getAddress());
            prefs.edit().putString("default_address_" + userId, gson.toJson(currentDefault)).apply();
            // Gửi intent về địa chỉ mặc định mới nhất
            Intent resultIntent = new Intent();
            resultIntent.putExtra("address_result", gson.toJson(currentDefault));
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            // Nếu không còn địa chỉ nào, xóa key default_address
            android.util.Log.d("AddressListActivity", "Xóa default_address vì không còn địa chỉ nào");
            prefs.edit().remove("default_address_" + userId).apply();
            // Gửi intent về báo không còn địa chỉ nào
            Intent resultIntent = new Intent();
            resultIntent.putExtra("address_result", "");
            setResult(RESULT_OK, resultIntent);
            finish();
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSetDefault(Address address) {
        for (Address a : addressList) a.setDefault(false);
        address.setDefault(true);
        saveAddresses();
        prefs.edit().putString("default_address_" + userId, gson.toJson(address)).apply();
        SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userId = loginPrefs.getString("userId", "");
        ApiService apiService = ApiClient.getApiService();
        User user = new User();
        user.setAddress(address.getAddress());
        apiService.updateUser(userId, user).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                // Thành công, có thể thông báo hoặc reload UI nếu cần
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // Thông báo lỗi nếu cần
            }
        });
        Intent intent = new Intent();
        intent.putExtra("address_result", gson.toJson(address));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Address address = gson.fromJson(data.getStringExtra("address_result"), Address.class);
            if (requestCode == REQ_ADD) {
                // Nếu chưa có địa chỉ nào là mặc định, set địa chỉ mới làm mặc định
                boolean hasDefault = false;
                for (Address a : addressList) {
                    if (a.isDefault()) {
                        hasDefault = true;
                        break;
                    }
                }
                if (!hasDefault) {
                    address.setDefault(true);
                }
                addressList.add(address);
                saveAddresses();
                adapter.notifyDataSetChanged();
            } else if (requestCode == REQ_EDIT) {
                for (int i = 0; i < addressList.size(); i++) {
                    if (addressList.get(i).getId().equals(address.getId())) {
                        if (address.isDefault()) for (Address a : addressList) a.setDefault(false);
                        addressList.set(i, address);
                        break;
                    }
                }
                saveAddresses();
                adapter.notifyDataSetChanged();
            }
        }
    }
} 