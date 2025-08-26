package com.example.shopbepoly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.shopbepoly.Adapter.DonMuaPagerAdapter;
import com.example.shopbepoly.fragment.ProfileFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DonMua extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DonMuaPagerAdapter pagerAdapter;
    private ImageView btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_don_mua);

        tabLayout = findViewById(R.id.tabLayoutDonMua);
        viewPager = findViewById(R.id.viewPagerDonMua);
        btn_back = findViewById(R.id.btnBack);

        pagerAdapter = new DonMuaPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        ImageView imgSearch = findViewById(R.id.imgsearch);
        imgSearch.setOnClickListener(v -> {
            Intent intent = new Intent(DonMua.this, TimDonHang.class);
            startActivity(intent);
        });


        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: tab.setText("Chờ xác nhận"); break;
                        case 1: tab.setText("Đang giao"); break;
                        case 2: tab.setText("Đã giao hàng"); break;
                        case 3: tab.setText("Đã hủy"); break;
                    }
                }).attach();
        String selectedTab = getIntent().getStringExtra("selectedTab");
        if (selectedTab != null) {
            if (selectedTab.equals("dagiao")) {
                viewPager.setCurrentItem(2, false); // 1 = Tab "Đã Giao", index bạn tự kiểm tra
            }
        }
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}