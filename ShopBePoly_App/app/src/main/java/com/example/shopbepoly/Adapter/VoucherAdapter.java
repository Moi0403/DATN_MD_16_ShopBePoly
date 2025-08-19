package com.example.shopbepoly.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.DTO.Voucher;
import com.example.shopbepoly.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    private Context context;
    private List<Voucher> voucherList;
    private OnVoucherClickListener listener;
    private SharedPreferences sharedPreferences;
    private static final String SAVED_VOUCHERS_KEY = "saved_vouchers";

    public interface OnVoucherClickListener {
        void onVoucherClick(Voucher voucher);
        void onSaveVoucherClick(Voucher voucher);
    }

    public VoucherAdapter(Context context, List<Voucher> voucherList) {
        this.context = context;
        this.voucherList = voucherList;
        this.sharedPreferences = context.getSharedPreferences("VoucherPrefs", Context.MODE_PRIVATE);
    }

    public void setOnVoucherClickListener(OnVoucherClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.bind(voucher);
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public class VoucherViewHolder extends RecyclerView.ViewHolder {
        private TextView tvVoucherTitle, tvVoucherDescription, tvVoucherCode, tvVoucherExpiry, btnSaveVoucher;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVoucherTitle = itemView.findViewById(R.id.tvVoucherTitle);
            tvVoucherDescription = itemView.findViewById(R.id.tvVoucherDescription);
            tvVoucherCode = itemView.findViewById(R.id.tvVoucherCode);
            tvVoucherExpiry = itemView.findViewById(R.id.tvVoucherExpiry);
            btnSaveVoucher = itemView.findViewById(R.id.btnSaveVoucher);
        }

        public void bind(Voucher voucher) {
            // Set voucher title based on discount type
            if ("percent".equals(voucher.getDiscountType())) {
                tvVoucherTitle.setText("Giảm " + (int)voucher.getDiscountValue() + "%");
            } else {
                tvVoucherTitle.setText("Giảm " + formatPrice(voucher.getDiscountValue()));
            }

            // Set description
            if (voucher.getMinOrderValue() > 0) {
                tvVoucherDescription.setText("Cho đơn hàng từ " + formatPrice(voucher.getMinOrderValue()));
            } else {
                tvVoucherDescription.setText(voucher.getDescription());
            }

            // Set voucher code
            tvVoucherCode.setText(voucher.getCode());

            // Set expiry date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvVoucherExpiry.setText("HSD: " + sdf.format(voucher.getEndDate()));

            // Check if voucher is saved
            Set<String> savedVouchers = sharedPreferences.getStringSet(SAVED_VOUCHERS_KEY, new HashSet<>());
            boolean isSaved = savedVouchers.contains(voucher.getId());

            if (isSaved) {
                btnSaveVoucher.setText("Đã lưu");
                btnSaveVoucher.setBackgroundResource(R.drawable.saved_button_bg);
                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.gray));
            } else {
                btnSaveVoucher.setText("Lưu");
                btnSaveVoucher.setBackgroundResource(R.drawable.save_button_bg);
                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.white));
            }

            // Handle save button click
            btnSaveVoucher.setOnClickListener(v -> {
                if (!isSaved) {
                    saveVoucher(voucher);
                    btnSaveVoucher.setText("Đã lưu");
                    btnSaveVoucher.setBackgroundResource(R.drawable.saved_button_bg);
                    btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.gray));
                    Toast.makeText(context, "Đã lưu mã giảm giá!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Mã đã được lưu trước đó", Toast.LENGTH_SHORT).show();
                }
            });

            // Handle item click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVoucherClick(voucher);
                }
            });
        }

        private void saveVoucher(Voucher voucher) {
            Set<String> savedVouchers = sharedPreferences.getStringSet(SAVED_VOUCHERS_KEY, new HashSet<>());
            savedVouchers = new HashSet<>(savedVouchers); // Create new set to avoid modification issues
            savedVouchers.add(voucher.getId());
            sharedPreferences.edit().putStringSet(SAVED_VOUCHERS_KEY, savedVouchers).apply();
        }

        private String formatPrice(double price) {
            if (price >= 1000000) {
                return String.format("%.0ftr", price / 1000000);
            } else if (price >= 1000) {
                return String.format("%.0fK", price / 1000);
            } else {
                return String.format("%.0fđ", price);
            }
        }
    }
}