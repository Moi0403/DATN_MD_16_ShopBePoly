package com.example.shopbepoly.nav;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.WebSocketManager;
import com.example.shopbepoly.DTO.LogoutResponse;
import com.example.shopbepoly.R;
import com.example.shopbepoly.databinding.ActivityHomeNavBinding;
import com.example.shopbepoly.fragment.CartFragment;
import com.example.shopbepoly.fragment.CategoryFragment;
import com.example.shopbepoly.fragment.FavoriteFragment;
import com.example.shopbepoly.fragment.HomeFragment;
import com.example.shopbepoly.fragment.ProfileFragment;

import java.util.HashMap;
import java.util.Map;

public class HomeNavScreen extends AppCompatActivity {

    ActivityHomeNavBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeNavBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.category) {
                replaceFragment(new CategoryFragment());
            } else if (id == R.id.favorite) {
                replaceFragment(new FavoriteFragment());
            } else if (id == R.id.cart) {
                replaceFragment(new CartFragment());
            } else if (id == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
    @Override
    protected void onResume() {
        super.onResume();

    }
//    @Override
//    protected void onStop() {
//        super.onStop();
//        WebSocketManager.onAppClose();
//        Log.d("HomeNavScreen", "onStop called, WebSocket disconnected");
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        WebSocketManager.onAppClose();
//        Log.d("HomeNavScreen", "onDestroy called, WebSocket disconnected");
//    }

    @Override
    protected void onStop() {
        super.onStop();
        // Ngắt WebSocket khi app background
        WebSocketManager.onAppClose();
        Log.d("HomeNavScreen", "onStop called, WebSocket disconnected");

        // Logout server khi app background (optional)
        logoutServerIfNeeded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ngắt WebSocket khi app bị destroy
        WebSocketManager.onAppClose();
        Log.d("HomeNavScreen", "onDestroy called, WebSocket disconnected");

        // Logout server khi app bị destroy (optional)
        logoutServerIfNeeded();
    }

    private void logoutServerIfNeeded() {
        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userId = preferences.getString("userId", "");

        if (userId != null && !userId.isEmpty()) {
            Map<String, String> body = new HashMap<>();
            body.put("userId", userId);

            ApiClient.getApiService().logout(body).enqueue(new retrofit2.Callback<LogoutResponse>() {
                @Override
                public void onResponse(retrofit2.Call<LogoutResponse> call, retrofit2.Response<LogoutResponse> response) {
                    Log.d("HomeNavScreen", "Server logout success");
                }

                @Override
                public void onFailure(retrofit2.Call<LogoutResponse> call, Throwable t) {
                    Log.d("HomeNavScreen", "Server logout failed: " + t.getMessage());
                }
            });
        }
    }
}
