package com.example.shopbepoly.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopbepoly.Chitietdonhang;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.R;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> ordList;
    private OrderListener listener;
    private String currentUserId;

    public interface OrderListener {
        void onDelete(String id);
    }

    public OrderAdapter(Context context, OrderListener listener) {
        this.context = context;
        this.listener = listener;
        this.ordList = new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);
    }

    // Gọi hàm này để set dữ liệu cho adapter
    public void setData(List<Order> originalList) {
        ordList.clear();
        if (originalList != null && currentUserId != null) {
            for (Order o : originalList) {
                if (currentUserId.equals(o.getId_user())) {
                    ordList.add(o);
                }
            }
        }
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
        Order ord = ordList.get(position);

        holder.tvmaDH.setText("Mã đơn hàng: " + ord.get_id());

        try {
            double billAmount = ord.getTotal();
            DecimalFormat formatter = new DecimalFormat("#,###.##");
            holder.tvthanhTien.setText("Tổng tiền: " + formatter.format(billAmount) + " đ");
        } catch (Exception e) {
            holder.tvthanhTien.setText("Tổng tiền: " + ord.getTotal() + " đ");
        }

        // Tính tổng số lượng sản phẩm từ chuỗi nameProduct
        int tongSoLuong = 0;
        String nameProductStr = ord.getNameproduct();
        if (nameProductStr != null) {
            String[] parts = nameProductStr.split(",");
            for (String part : parts) {
                int start = part.indexOf("(x");
                int end = part.indexOf(")");
                if (start != -1 && end != -1 && end > start) {
                    try {
                        int sl = Integer.parseInt(part.substring(start + 2, end).trim());
                        tongSoLuong += sl;
                    } catch (Exception ignored) {}
                }
            }
        }

        holder.tvSoLuongSP.setText("Số lượng sản phẩm: " + tongSoLuong);
        holder.tvngayMua.setText("Ngày mua: " + ord.getDate());
        holder.tvTT.setText("Trạng thái: " + ord.getStatus());

        // Hiển thị hoặc ẩn nút hủy
        String status = ord.getStatus();
        if ("Đã hủy".equalsIgnoreCase(status) || "Đã giao".equalsIgnoreCase(status) || "Hoàn thành".equalsIgnoreCase(status)) {
            holder.btnHuy.setVisibility(View.GONE);
        } else {
            holder.btnHuy.setVisibility(View.VISIBLE);
            holder.btnHuy.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(ord.get_id());
                }
            });
        }

        // Mở chi tiết đơn hàng khi click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Chitietdonhang.class);
            intent.putExtra("order_id", ord.get_id());
            intent.putExtra("order_status", ord.getStatus());
            intent.putExtra("order_address", ord.getAddress());
            intent.putExtra("order_bill", ord.getTotal());
            intent.putExtra("order_date", ord.getDate());
            intent.putExtra("order_pay", ord.getPay());
            intent.putExtra("order_nameproduct", ord.getNameproduct());
            intent.putExtra("order_image", new Gson().toJson(ord.getImg_oder()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ordList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvmaDH, tvthanhTien, tvngayMua, tvTT, tvSoLuongSP;
        MaterialButton btnHuy;

        @SuppressLint("WrongViewCast")
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvmaDH = itemView.findViewById(R.id.tvmaDH);
            tvthanhTien = itemView.findViewById(R.id.tvthanhTien);
            tvSoLuongSP = itemView.findViewById(R.id.tvSoLuongSP);
            tvngayMua = itemView.findViewById(R.id.tvngayMua);
            tvTT = itemView.findViewById(R.id.tvTT);
            btnHuy = itemView.findViewById(R.id.btnHuy);
        }
    }
}
