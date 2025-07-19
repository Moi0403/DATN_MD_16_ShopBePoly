package com.example.shopbepoly.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.shopbepoly.fragment.ChoXacNhanFragment;
import com.example.shopbepoly.fragment.DaGiaoFragment;
import com.example.shopbepoly.fragment.DaHuyFragment;
import com.example.shopbepoly.fragment.DangGiaoFragment;
import com.example.shopbepoly.fragment.DangLayHangFragment;
import com.example.shopbepoly.fragment.LayHangThanhCongFragment;

public class DonMuaPagerAdapter extends FragmentStateAdapter {

    public DonMuaPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new ChoXacNhanFragment();
            case 1: return new DangLayHangFragment();
            case 2: return new LayHangThanhCongFragment();
            case 3: return new DangGiaoFragment();
            case 4: return new DaGiaoFragment();
            case 5: return new DaHuyFragment();
            default: return new ChoXacNhanFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
