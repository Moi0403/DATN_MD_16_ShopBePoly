package com.example.shopbepoly.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.RecyclerView;


import com.example.shopbepoly.ChinhSachvaQuyenRiengTu;
import com.example.shopbepoly.DieuKhoanvaDieuKien;
import com.example.shopbepoly.DoiMatKhau;
import com.example.shopbepoly.Gioithieu;
import com.example.shopbepoly.Lichsugiaodich;
import com.example.shopbepoly.LienHe;
import com.example.shopbepoly.MainActivity;
import com.example.shopbepoly.Profile;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.LoginScreen;
import com.example.shopbepoly.ThongTinCaNhan;


public class ProfileFragment extends Fragment {

    private TextView txtThongtincanhan, txtLichsugiaodich, txtGioiThieu, txtDoimatkhau, txtLienhe, txtDieuKhoan, txtChinhsach, txtLogout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        txtThongtincanhan = view.findViewById(R.id.txtThongtincanhan);
        txtLichsugiaodich = view.findViewById(R.id.txtLichsugiaodich);
        txtGioiThieu = view.findViewById(R.id.txtGioithieu);
        txtDoimatkhau = view.findViewById(R.id.txtDoimatkhau);
        txtLienhe = view.findViewById(R.id.txtLienhe);
        txtDieuKhoan = view.findViewById(R.id.txtDieukhoan);
        txtChinhsach = view.findViewById(R.id.txtChinhsach);
        txtLogout = view.findViewById(R.id.txtLogout);

        txtThongtincanhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ThongTinCaNhan.class));
            }
        });
        txtLichsugiaodich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Lichsugiaodich.class));
            }
        });
        txtDoimatkhau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DoiMatKhau.class));
            }
        });

        txtGioiThieu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Gioithieu.class));
            }
        });
        txtLienhe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(getActivity(),LienHe.class));
            }
        });

        txtDieuKhoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DieuKhoanvaDieuKien.class));
            }
        });
        txtChinhsach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChinhSachvaQuyenRiengTu.class));
            }
        });
        txtLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
        return view;
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_logout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(view -> {
            SharedPreferences preferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
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
            Intent intent = new Intent(getActivity(), LoginScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dialog.dismiss();
        });

        btnNo.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }
}