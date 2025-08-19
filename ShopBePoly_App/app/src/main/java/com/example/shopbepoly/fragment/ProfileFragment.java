package com.example.shopbepoly.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.example.shopbepoly.ChinhSachvaQuyenRiengTu;
import com.example.shopbepoly.DieuKhoanvaDieuKien;
import com.example.shopbepoly.DoiMatKhau;
import com.example.shopbepoly.DonMua;
import com.example.shopbepoly.Donhang;
import com.example.shopbepoly.Gioithieu;
import com.example.shopbepoly.Lichsugiaodich;
import com.example.shopbepoly.LienHe;
import com.example.shopbepoly.MainActivity;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.LoginScreen;
import com.example.shopbepoly.ThongTinCaNhan;
import com.example.shopbepoly.VoucherActivity;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView txtThongtincanhan, txtLichsugiaodich, txtGioiThieu, txtDoimatkhau, txtLienhe, txtDieuKhoan, txtChinhsach, txtLogout, txtDonmua,txtVoucher;
    private ImageView imgAvatar;
    private TextView txtName, txtEmail;

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
        imgAvatar = view.findViewById(R.id.imgAvatar);
        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtDonmua = view.findViewById(R.id.txtDonmua);
        txtVoucher = view.findViewById(R.id.txtVoucher);

        loadUserProfile();

        txtThongtincanhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ThongTinCaNhan.class);
                startActivityForResult(intent, 1001);
            }
        });
        txtDonmua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DonMua.class));
            }
        });
        txtVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang VoucherActivity với tab "Đã lưu" (tab index = 1)
                Intent intent = new Intent(getActivity(), VoucherActivity.class);
                intent.putExtra("selectedTab", 1); // 0: Available, 1: Saved, 2: Used
                startActivity(intent);
            }
        });
        txtLichsugiaodich.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DonMua.class);
            intent.putExtra("selectedTab", "dagiao"); // Truyền key để mở tab Đã Giao
            startActivity(intent);
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
                startActivity(new Intent(getActivity(), LienHe.class));
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

    // Trong onCreateView -> thay bằng gọi hàm loadUserProfile()
    private void loadUserProfile() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        ApiService apiService = ApiClient.getApiService();
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (User user : response.body()) {
                        if (user.getId().equals(userId)) {
                            txtName.setText(user.getName() != null ? user.getName() : "");
                            txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                            String avatarFileName = user.getAvatar(); // hoặc getAvtUser() tùy bạn đặt tên
                            if (avatarFileName != null && !avatarFileName.isEmpty()) {
                                String fullUrl = ApiClient.IMAGE_URL + avatarFileName + "?t=" + System.currentTimeMillis();
                                Glide.with(ProfileFragment.this)
                                        .load(fullUrl)
                                        .apply(new RequestOptions()
                                                .transform(new CircleCrop())
                                                .placeholder(R.drawable.ic_avatar)
                                                .error(R.drawable.ic_avatar))
                                        .into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(R.drawable.ic_avatar);
                            }

                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            loadUserProfile(); // ✅ Reload lại avatar, tên, email
        }
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