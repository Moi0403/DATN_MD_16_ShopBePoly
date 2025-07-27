package com.example.shopbepoly.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Chitietdonhang;
import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.ChiTietSanPham;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> ordList;

    private Runnable onOrderCancelled;

    public OrderAdapter(Context context, List<Order> list, Runnable onOrderCancelled) {
        this.context = context;
        this.ordList = list;
        this.onOrderCancelled = onOrderCancelled;
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
        if ("Đã hủy".equalsIgnoreCase(order.getStatus())) {
            holder.tvLydo.setVisibility(View.VISIBLE);
            holder.tvLydo.setText("Lý do hủy: " + order.getCancelReason());
        } else {
            holder.tvLydo.setVisibility(View.GONE);
        }


        holder.btnChiTiet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Chitietdonhang.class);
                intent.putExtra("order", order);
                context.startActivity(intent);
            }
        });
        // Hủy đơn hàng
        holder.btnHuy.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_cancel_order, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogView);

            builder.setPositiveButton("Xác nhận hủy", (dialog, which) -> {
                RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupReasons);
                int selectedId = radioGroup.getCheckedRadioButtonId();

                if (selectedId != -1) {
                    RadioButton selectedRadio = dialogView.findViewById(selectedId);
                    String reason = selectedRadio.getText().toString();

                    Order updateOrder = new Order();
                    updateOrder.set_id(order.get_id());
                    updateOrder.setStatus("Đã hủy");
                    updateOrder.setCancelReason(reason); // 👈 Gửi lý do lên server

                    cancelOrder(updateOrder, holder.getAdapterPosition());
                    Toast.makeText(context, "Lý do hủy: " + reason, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Vui lòng chọn lý do hủy", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Hủy bỏ", null);
            builder.show();
        });





        // Xử lý hiển thị các nút tùy theo trạng thái đơn hàng
        if ("Đang xử lý".equalsIgnoreCase(order.getStatus())) {
            holder.btnHuy.setVisibility(View.VISIBLE);
            holder.btnNhan.setVisibility(View.GONE);
            holder.btnMuaLai.setVisibility(View.GONE);
        } else if ("Đang giao".equalsIgnoreCase(order.getStatus())) {
            holder.btnNhan.setVisibility(View.VISIBLE);
            holder.btnHuy.setVisibility(View.GONE);
            holder.btnMuaLai.setVisibility(View.GONE);
        } else if ("Đã hủy".equalsIgnoreCase(order.getStatus())) {
            holder.btnMuaLai.setVisibility(View.VISIBLE);
            holder.btnHuy.setVisibility(View.GONE);
            holder.btnNhan.setVisibility(View.GONE);
        } else if ("Đã giao".equalsIgnoreCase(order.getStatus())) {
            holder.btnMuaLai.setVisibility(View.VISIBLE);
            holder.btnHuy.setVisibility(View.GONE);
            holder.btnNhan.setVisibility(View.GONE);
        } else {
            holder.btnHuy.setVisibility(View.GONE);
            holder.btnNhan.setVisibility(View.GONE);
            holder.btnMuaLai.setVisibility(View.GONE);
        }


        holder.btnNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setIcon(R.drawable.thongbao);
                builder.setTitle("Thông báo");
                builder.setMessage("Bạn chắc chắn đã nhận được hàng ?");
                builder.setPositiveButton("Đúng", (dialog, which) -> {
                    Order order1 = new Order();
                    order1.set_id(order.get_id());
                    order1.setStatus("Đã giao");
                    UpdateOrder(order1);
                });

                builder.setNegativeButton("Hủy", null);
                builder.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return ordList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvmaDH, tvthanhTien, tvngayMua, tvTT,tvLydo, tvSoLuongSP;
        Button btnHuy, btnChiTiet, btnNhan,btnMuaLai;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvmaDH = itemView.findViewById(R.id.tvmaDH);
            tvthanhTien = itemView.findViewById(R.id.tvthanhTien);
            tvSoLuongSP = itemView.findViewById(R.id.tvSoLuongSP);
            tvngayMua = itemView.findViewById(R.id.tvngayMua);
            tvTT = itemView.findViewById(R.id.tvTT);
            tvLydo = itemView.findViewById(R.id.tvLydo);
            btnHuy = itemView.findViewById(R.id.btnHuy);
            btnChiTiet = itemView.findViewById(R.id.btnChitiet);
            btnNhan = itemView.findViewById(R.id.btnNhan);
            btnMuaLai = itemView.findViewById(R.id.btnMuaLai);
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

    private void UpdateOrder(Order order){
        ApiService apiService = ApiClient.getApiService();
        Call<Order> call = apiService.upStatus(order.get_id(), order);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    // Cập nhật lại danh sách hoặc giao diện nếu cần
                    notifyDataSetChanged(); // Làm mới adapter
                } else {
                    Toast.makeText(context, "Cập nhật thất bại: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {

            }
        });
    }
    private void cancelOrder(Order order, int position) {
        ApiService apiService = ApiClient.getApiService();
        apiService.upStatus(order.get_id(), order).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Hủy đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    ordList.remove(position);
                    notifyItemRemoved(position);
                    if (onOrderCancelled != null) {
                        onOrderCancelled.run(); // Fragment reload lại list nếu cần
                    }
                } else {
                    Toast.makeText(context, "Hủy đơn thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
