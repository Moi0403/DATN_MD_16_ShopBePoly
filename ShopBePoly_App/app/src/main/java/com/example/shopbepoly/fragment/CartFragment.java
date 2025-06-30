package com.example.shopbepoly.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.CartAdapter;
import com.example.shopbepoly.Adapter.ProductAdapter;
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {
    private RecyclerView rc_cart;
    private List<Cart> list_cart = new ArrayList<>();
    private CartAdapter cartAdapter;
    public TextView tvTotal;
    public CheckBox checkbox_select_all;
    private ImageView imv_delAll;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        rc_cart = view.findViewById(R.id.recycler_cart);
        tvTotal = view.findViewById(R.id.text_total);
        checkbox_select_all = view.findViewById(R.id.checkbox_select_all);
        imv_delAll = view.findViewById(R.id.imv_deleteAll);


        SharedPreferences prefs = requireContext().getSharedPreferences("CartPrefs", Context.MODE_PRIVATE);
        boolean isSelectAll = prefs.getBoolean("select_all_checked", false);
        checkbox_select_all.setChecked(isSelectAll);
        if (cartAdapter != null) {
            cartAdapter.selectAll(isSelectAll);
        }


        SharedPreferences sharedPreferences = getContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        Log.d("CartFragment", "UserId: " + userId);

        if (getContext() == null) {
            Log.e("CartFragment", "Context is null in onCreateView");
            Toast.makeText(getActivity(), "Lỗi: Context không khả dụng", Toast.LENGTH_SHORT).show();
            return view;
        }
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            return view;
        }
        rc_cart.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(getContext(), list_cart, CartFragment.this);
        rc_cart.setAdapter(cartAdapter);
        LoadCart(userId);
        imv_delAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDelAll(userId);
            }
        });

        checkbox_select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = checkbox_select_all.isChecked();
                cartAdapter.selectAll(isChecked);
                SharedPreferences prefs = requireContext().getSharedPreferences("CartPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("select_all_checked", isChecked); // isChecked là trạng thái của checkbox_all
                editor.apply();
            }
        });


        return view;
    }


    public void LoadCart(String userId){
        ApiService apiService = ApiClient.getApiService();
        Call<List<Cart>> call = apiService.getCart(userId);
        call.enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("LoadCart", "Response JSON: " + new Gson().toJson(response.body()));
                    Log.d("LoadCart", "Số lượng item: " + response.body().size());
                    list_cart.clear();
                    list_cart.addAll(response.body());
                    cartAdapter.notifyDataSetChanged();
                } else {
                    Log.e("LoadCart", "Response không thành công: " + response.code() + ", Message: " + response.message());
                    Log.e("LoadCart", "Raw response: " + response.raw().toString());
                    Toast.makeText(getContext(), "Hiện thị GH ko thành công", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                Log.e("LoadCart", "Lỗi khi gọi API: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ShowDelAll(String userId){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setIcon(R.drawable.thongbao);
        builder.setTitle("Thông báo");
        builder.setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng không ?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            ApiService apiService = ApiClient.getApiService();
            Call<ResponseBody> call = apiService.deleteAllCart(userId);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Đã xóa toàn bộ giỏ hàng", Toast.LENGTH_SHORT).show();
                        LoadCart(userId);
                        checkbox_select_all.setChecked(false);
                        tvTotal.setText(String.format("Giá: " + "%,d đ",0));
                    } else {
                        Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

}