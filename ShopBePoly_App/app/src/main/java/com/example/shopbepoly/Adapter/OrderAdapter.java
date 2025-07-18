package com.example.shopbepoly.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopbepoly.Chitietdonhang;
import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.ChiTietSanPham;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> ordList;

    public OrderAdapter(Context context, List<Order> list) {
        this.context = context;
        this.ordList = list;
    }

    public void setData(List<Order> list) {
        this.ordList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donhang, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = ordList.get(position);

        holder.tvmaDH.setText("Mã đơn hàng: " +order.get_id());
        String totalStr = order.getTotal();
        int totalValue = 0;
        try {
            if (totalStr != null && !totalStr.isEmpty()) {
                totalValue = Integer.parseInt(totalStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        holder.tvthanhTien.setText(String.format("Giá: %,d đ", totalValue));
        holder.tvSoLuongSP.setText("Tổng số lượng sản phẩm: " +order.getQuantity_order());
        holder.tvngayMua.setText("Ngày: " + formatDate(order.getDate()));
        holder.tvTT.setText("Trạng thái: " +order.getStatus());

        holder.btnChiTiet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Chitietdonhang.class);
                intent.putExtra("order", order);
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return ordList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvmaDH, tvthanhTien, tvngayMua, tvTT, tvSoLuongSP;
        Button btnHuy, btnChiTiet;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvmaDH = itemView.findViewById(R.id.tvmaDH);
            tvthanhTien = itemView.findViewById(R.id.tvthanhTien);
            tvSoLuongSP = itemView.findViewById(R.id.tvSoLuongSP);
            tvngayMua = itemView.findViewById(R.id.tvngayMua);
            tvTT = itemView.findViewById(R.id.tvTT);
            btnHuy = itemView.findViewById(R.id.btnHuy);
            btnChiTiet = itemView.findViewById(R.id.btnChitiet);
        }
    }

    private String formatDate(String isoDate) {
        try {
            // Định dạng ban đầu: ISO 8601 (UTC)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Định dạng muốn hiển thị (giờ Việt Nam)
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

            Date date = inputFormat.parse(isoDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return isoDate; // fallback nếu lỗi
        }
    }

}
