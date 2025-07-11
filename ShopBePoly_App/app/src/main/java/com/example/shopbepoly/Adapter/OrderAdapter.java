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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // Tính tổng số lượng sản phẩm
        final int tongSoLuong;
        int tempSoLuong = 0;
        String nameProductStr = ord.getNameproduct();
        if (nameProductStr != null) {
            Pattern pattern = Pattern.compile("x(\\d+)");
            Matcher matcher = pattern.matcher(nameProductStr);
            while (matcher.find()) {
                try {
                    int sl = Integer.parseInt(matcher.group(1));
                    tempSoLuong += sl;
                } catch (Exception ignored) {}
            }
        }
        tongSoLuong = tempSoLuong;

        holder.tvSoLuongSP.setText("Số lượng sản phẩm: " + tongSoLuong);
        holder.tvngayMua.setText("Ngày mua: " + ord.getDate());
        holder.tvTT.setText("Trạng thái: " + ord.getStatus());

        // Hiển thị nút huỷ nếu trạng thái phù hợp
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

        // 👉 Truyền dữ liệu sang màn ChiTietDonHang
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Chitietdonhang.class);
            intent.putExtra("order_id", ord.get_id());
            intent.putExtra("order_status", ord.getStatus());
            intent.putExtra("order_address", ord.getAddress());
            intent.putExtra("order_bill", ord.getTotal());
            intent.putExtra("order_date", ord.getDate());
            intent.putExtra("order_pay", ord.getPay());
            intent.putExtra("order_nameproduct", ord.getNameproduct());
            intent.putExtra("order_quantity", String.valueOf(tongSoLuong));
            intent.putExtra("order_idproduct", ord.getId_product()); // ✅ Truyền thêm id sản phẩm

            // 👉 Tách ảnh thành danh sách
            ArrayList<String> imageList = new ArrayList<>();
            String rawImages = ord.getImg_oder();
            if (rawImages != null && !rawImages.isEmpty()) {
                String[] split = rawImages.split(",");
                for (String img : split) {
                    imageList.add(img.trim());
                }
            }

            intent.putStringArrayListExtra("order_image_list", imageList);

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