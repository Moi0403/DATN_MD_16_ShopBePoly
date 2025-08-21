package com.example.shopbepoly.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.DTO.Voucher;
import com.example.shopbepoly.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VoucherSelectionAdapter extends RecyclerView.Adapter<VoucherSelectionAdapter.VoucherViewHolder> {

    private static final String TAG = "VoucherAdapter";
    private Context context;
    private List<Voucher> voucherList;
    private double orderTotal;
    private OnVoucherSelectionListener listener;

    public interface OnVoucherSelectionListener {
        void onVoucherSelected(Voucher voucher);
    }

    public VoucherSelectionAdapter(Context context, List<Voucher> voucherList, double orderTotal) {
        this.context = context;
        this.voucherList = voucherList;
        this.orderTotal = orderTotal;
        Log.d(TAG, "Adapter created with orderTotal: " + orderTotal);
    }

    public void setOnVoucherSelectionListener(OnVoucherSelectionListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Voucher> newVoucherList) {
        this.voucherList = newVoucherList;
        notifyDataSetChanged();
        Log.d(TAG, "Data updated, voucher count: " + (newVoucherList != null ? newVoucherList.size() : 0));
    }

    public void updateOrderTotal(double newOrderTotal) {
        this.orderTotal = newOrderTotal;
        notifyDataSetChanged();
        Log.d(TAG, "Order total updated to: " + newOrderTotal);
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher_selection, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);

        Log.d(TAG, "Binding voucher: " + voucher.getCode() + " - OrderTotal: " + orderTotal + " - MinOrder: " + voucher.getMinOrderValue());

        // Set voucher code
        holder.tvVoucherCode.setText(voucher.getCode());

        // Set voucher title (discount info)
        String discountText = getDiscountText(voucher);
        holder.tvVoucherTitle.setText(discountText);

        // Set description with conditions
        String description = getDescriptionText(voucher);
        holder.tvVoucherDescription.setText(description);

        // Set expiry date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String expiryText = "Hết hạn: " + sdf.format(voucher.getEndDate());
        holder.tvExpiryDate.setText(expiryText);

        // Set usage count
        int remainingUsage = voucher.getUsageLimit() - voucher.getUsedCount();
        String usageText = String.format("Còn lại: %d/%d", remainingUsage, voucher.getUsageLimit());
        holder.tvUsageCount.setText(usageText);

        // Check if voucher is applicable - CHỈ CHECK TỔNG TIỀN VÀ ĐƠN TỐI THIỂU
        boolean isApplicable = orderTotal >= voucher.getMinOrderValue();

        Log.d(TAG, "Voucher " + voucher.getCode() + " - Applicable: " + isApplicable +
                " (OrderTotal: " + orderTotal + " >= MinOrder: " + voucher.getMinOrderValue() + ")");

        // Calculate actual discount for display
        double actualDiscount = calculateDiscountForOrder(voucher, orderTotal);

        // Update UI based on applicability
        updateVoucherItemUI(holder, voucher, isApplicable, actualDiscount);

        // Set click listener
        holder.btnUseVoucher.setOnClickListener(v -> {
            Log.d(TAG, "Button clicked for voucher: " + voucher.getCode() + ", applicable: " + isApplicable);
            if (listener != null && isApplicable) {
                listener.onVoucherSelected(voucher);
            }
        });

        // Set voucher icon
        holder.ivVoucherIcon.setImageResource(R.drawable.ic_voucher_ticket);
    }

    /**
     * Tính toán số tiền giảm giá thực tế cho đơn hàng
     */
    private double calculateDiscountForOrder(Voucher voucher, double orderTotal) {
        if (orderTotal < voucher.getMinOrderValue()) {
            return 0;
        }

        double discount = 0;
        if ("percent".equals(voucher.getDiscountType()) || "percentage".equals(voucher.getDiscountType())) {
            // Giảm giá theo phần trăm
            discount = orderTotal * (voucher.getDiscountValue() / 100.0);
        } else {
            // Giảm giá cố định
            discount = voucher.getDiscountValue();
        }

        // Đảm bảo không vượt quá tổng tiền đơn hàng
        double finalDiscount = Math.min(discount, orderTotal);

        Log.d(TAG, "Calculated discount for " + voucher.getCode() + ": " + finalDiscount +
                " (Original: " + discount + ", OrderTotal: " + orderTotal + ")");

        return finalDiscount;
    }

    /**
     * Cập nhật giao diện voucher item dựa trên trạng thái có thể sử dụng hay không
     */
    private void updateVoucherItemUI(VoucherViewHolder holder, Voucher voucher, boolean isApplicable, double actualDiscount) {
        if (isApplicable) {
            // Voucher có thể sử dụng
            holder.cardVoucher.setAlpha(1.0f);
            holder.btnUseVoucher.setEnabled(true);
            holder.btnUseVoucher.setText("Dùng ngay");
            holder.btnUseVoucher.setBackgroundResource(R.drawable.bg_button_rounded);

            try {
                holder.btnUseVoucher.setBackgroundTintList(context.getResources().getColorStateList(R.color.orange));
            } catch (Exception e) {
                Log.w(TAG, "Could not set background tint: " + e.getMessage());
            }

            // Hiển thị số tiền tiết kiệm thực tế
            String statusText = String.format("✓ Tiết kiệm %s cho đơn hàng này", formatCurrency(actualDiscount));
            holder.tvStatus.setText(statusText);

            try {
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.star_gold));
            } catch (Exception e) {
                holder.tvStatus.setTextColor(0xFF4CAF50); // Green fallback
            }

            Log.d(TAG, "UI updated for applicable voucher: " + voucher.getCode());

        } else {
            // Voucher không thể sử dụng - chỉ do không đủ đơn tối thiểu
            holder.cardVoucher.setAlpha(0.6f);
            holder.btnUseVoucher.setEnabled(false);
            holder.btnUseVoucher.setText("Không đủ điều kiện");

            try {
                holder.btnUseVoucher.setBackgroundResource(R.drawable.bg_voucher_disabled);
            } catch (Exception e) {
                Log.w(TAG, "Could not set disabled background: " + e.getMessage());
            }

            // Tính số tiền cần mua thêm
            double needed = voucher.getMinOrderValue() - orderTotal;
            String statusText = String.format("Cần mua thêm %s để đạt tối thiểu %s",
                    formatCurrency(needed), formatCurrency(voucher.getMinOrderValue()));
            holder.tvStatus.setText(statusText);

            try {
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.primary_red));
            } catch (Exception e) {
                holder.tvStatus.setTextColor(0xFFE53E3E); // Red fallback
            }

            Log.d(TAG, "UI updated for non-applicable voucher: " + voucher.getCode() +
                    " (Need more: " + needed + ")");
        }
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    /**
     * Lấy text hiển thị thông tin giảm giá
     */
    private String getDiscountText(Voucher voucher) {
        if ("percent".equals(voucher.getDiscountType()) || "percentage".equals(voucher.getDiscountType())) {
            return String.format("Giảm %d%%", (int) voucher.getDiscountValue());
        } else {
            return String.format("Giảm %s", formatCurrency(voucher.getDiscountValue()));
        }
    }

    /**
     * Lấy text mô tả voucher với các điều kiện
     */
    private String getDescriptionText(Voucher voucher) {
        StringBuilder description = new StringBuilder();

        // Thêm mô tả cơ bản nếu có
        if (voucher.getDescription() != null && !voucher.getDescription().trim().isEmpty()) {
            description.append(voucher.getDescription());
        }

        // Thêm điều kiện đơn hàng tối thiểu
        if (voucher.getMinOrderValue() > 0) {
            if (description.length() > 0) {
                description.append(" • ");
            }
            description.append("Áp dụng cho đơn từ ").append(formatCurrency(voucher.getMinOrderValue()));
        }

        return description.length() > 0 ? description.toString() : "Không có điều kiện đặc biệt";
    }

    /**
     * Format số tiền theo định dạng tiền tệ
     */
    private String formatCurrency(double amount) {
        try {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            return formatter.format(amount) + "₫";
        } catch (Exception e) {
            Log.w(TAG, "Error formatting currency: " + e.getMessage());
            return String.valueOf((long)amount) + "₫";
        }
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        CardView cardVoucher;
        ImageView ivVoucherIcon;
        TextView tvVoucherCode;
        TextView tvVoucherTitle;
        TextView tvVoucherDescription;
        TextView tvExpiryDate;
        TextView tvUsageCount;
        TextView tvStatus;
        Button btnUseVoucher;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            cardVoucher = itemView.findViewById(R.id.cardVoucher);
            ivVoucherIcon = itemView.findViewById(R.id.ivVoucherIcon);
            tvVoucherCode = itemView.findViewById(R.id.tvVoucherCode);
            tvVoucherTitle = itemView.findViewById(R.id.tvVoucherTitle);
            tvVoucherDescription = itemView.findViewById(R.id.tvVoucherDescription);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
            tvUsageCount = itemView.findViewById(R.id.tvUsageCount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnUseVoucher = itemView.findViewById(R.id.btnUseVoucher);
        }
    }
}