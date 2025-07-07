package com.example.shopbepoly.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopbepoly.Chitietdonhang;
import com.google.android.material.button.MaterialButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> ordList;
    private OrderListener listener;

    public interface OrderListener {
        void onDelete(String id);
    }

    public OrderAdapter(Context context, List<Order> ordList, OrderListener listener) {
        this.context = context;
        this.ordList = ordList;
        this.listener = listener;
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

        //Hiển thị thông tin đơn hàng
        holder.tvmaDH.setText("Mã đơn hàng:" + ord.get_id());
        //format giá tiền
        DecimalFormat formatter = new DecimalFormat("#,###");
        try {
            double billAmount = ord.getBillAsDouble();
            holder.tvthanhTien.setText("Giá:" + formatter.format(billAmount) + " đ");
        } catch (Exception e) {
            holder.tvthanhTien.setText("Giá:" + ord.getBill() + " đ");
        }
        holder.tvngayMua.setText("Ngày mua:" + ord.getDate());
        holder.tvTT.setText("Trạng thái: " + ord.getStatus());

        //xử lý nút hủy
        String status = ord.getStatus();
        if ("Đã hủy".equalsIgnoreCase(status) || "Đã giao".equalsIgnoreCase(status) || "Hoàn thành".equalsIgnoreCase(status)){
            holder.btnHuy.setVisibility(View.GONE);
        } else {
            holder.btnHuy.setVisibility(View.VISIBLE);
            holder.btnHuy.setOnClickListener(v -> {
                if (listener != null){
                    listener.onDelete(ord.get_id());
                }
            });
        }

//        holder.btnHuy.setOnClickListener(v -> {
//            new androidx.appcompat.app.AlertDialog.Builder(context)
//                .setTitle("Xác nhận hủy đơn hàng")
//                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
//                .setPositiveButton("Có", (dialog, which) -> {
//                    listener.onDelete(ord.get_id());
//                })
//                .setNegativeButton("Không", null)
//                .show();
//        });
//
//        // Ẩn nút hủy nếu trạng thái đã là 'Đã hủy'
//        if ("Đã hủy".equalsIgnoreCase(ord.getStatus())) {
//            holder.btnHuy.setVisibility(View.GONE);
//            holder.tvTT.setText("Đã hủy");
//        } else {
//            holder.btnHuy.setVisibility(View.VISIBLE);
//        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Chitietdonhang.class);
            intent.putExtra("order_id", ord.get_id());
            intent.putExtra("order_status", ord.getStatus());
            intent.putExtra("order_address", ord.getAddress());
            intent.putExtra("order_bill", ord.getBill());
            intent.putExtra("order_date", ord.getDate());
            intent.putExtra("order_pay", ord.getPay());
            intent.putExtra("order_nameproduct", ord.getNameproduct());
            intent.putExtra("order_image", ord.getFirstImage());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ordList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvmaDH, tvthanhTien, tvngayMua, tvTT;
        MaterialButton btnHuy;

        @SuppressLint("WrongViewCast")
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvmaDH = itemView.findViewById(R.id.tvmaDH);
            tvthanhTien = itemView.findViewById(R.id.tvthanhTien);
            tvngayMua = itemView.findViewById(R.id.tvngayMua);
            tvTT = itemView.findViewById(R.id.tvTT);
            btnHuy = itemView.findViewById(R.id.btnHuy);
        }
    }

//    private void detailDonHang(Order ord) {
//
//        tv.setText(xe.getTen_xe_ph41100());
//        DecimalFormat formatter = new DecimalFormat("#,###");
//        tvGiaXe.setText("Giá: " + formatter.format(xe.getGia_ban_ph41100()) + " đ");
//        tvMauXe.setText("Màu: " + xe.getMau_sac_ph41100());
//        tvMoTaXe.setText("Mô tả: " + xe.getMo_ta_ph41100());
//
//        builder.setView(dialogView);
//
//        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
//
//        builder.show();
//    }
}



