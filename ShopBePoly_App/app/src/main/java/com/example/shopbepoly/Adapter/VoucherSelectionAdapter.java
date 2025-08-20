package com.example.shopbepoly.Adapter;

import android.content.Context;
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
    }

    public void setOnVoucherSelectionListener(OnVoucherSelectionListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Voucher> newVoucherList) {
        this.voucherList = newVoucherList;
        notifyDataSetChanged();
    }

    public void updateOrderTotal(double newOrderTotal) {
        this.orderTotal = newOrderTotal;
        notifyDataSetChanged();
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
        String usageText = String.format("Còn lại: %d/%d",
                voucher.getUsageLimit() - voucher.getUsedCount(),
                voucher.getUsageLimit());
        holder.tvUsageCount.setText(usageText);

        // Calculate actual discount for this order
        double actualDiscount = calculateDiscountForOrder(voucher, orderTotal);

        // Check if voucher is applicable
        boolean isApplicable = orderTotal >= voucher.getMinOrderValue() &&
                voucher.isAvailable() &&
                actualDiscount > 0;

        // Update UI based on applicability
        updateVoucherItemUI(holder, voucher, isApplicable, actualDiscount);

        // Set click listener
        holder.btnUseVoucher.setOnClickListener(v -> {
            if (listener != null && isApplicable) {
                listener.onVoucherSelected(voucher);
            }
        });

        // Set voucher icon based on discount type
        if ("percent".equals(voucher.getDiscountType()) || "percentage".equals(voucher.getDiscountType())) {
            holder.ivVoucherIcon.setImageResource(R.drawable.ic_voucher_ticket);
        } else {
            holder.ivVoucherIcon.setImageResource(R.drawable.ic_voucher_ticket);
        }
    }

    private void updateVoucherItemUI(VoucherViewHolder holder, Voucher voucher, boolean isApplicable, double actualDiscount) {
        if (isApplicable) {
            // Voucher can be used
            holder.cardVoucher.setAlpha(1.0f);
            holder.btnUseVoucher.setEnabled(true);
            holder.btnUseVoucher.setText("Dùng ngay");
            holder.btnUseVoucher.setBackgroundResource(R.drawable.bg_button_rounded);
            holder.btnUseVoucher.setBackgroundTintList(context.getResources().getColorStateList(R.color.orange));

            // Show applicable status with actual savings
            String statusText = String.format("✓ Tiết kiệm %s cho đơn hàng này", formatCurrency(actualDiscount));
            holder.tvStatus.setText(statusText);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.star_gold));

        } else {
            // Voucher cannot be used
            holder.cardVoucher.setAlpha(0.6f);
            holder.btnUseVoucher.setEnabled(false);

            // Determine why voucher cannot be used
            String statusText;
            int statusColor;

            if (!voucher.isAvailable()) {
                holder.btnUseVoucher.setText("Hết hạn");
                statusText = "❌ Voucher đã hết hạn hoặc hết lượt";
                statusColor = R.color.primary_red;
            } else if (orderTotal < voucher.getMinOrderValue()) {
                holder.btnUseVoucher.setText("Không đủ điều kiện");
                double needed = voucher.getMinOrderValue() - orderTotal;
                statusText = String.format("Cần mua thêm %s để đạt tối thiểu %s",
                        formatCurrency(needed), formatCurrency(voucher.getMinOrderValue()));
                statusColor = R.color.primary_red;
            } else {
                holder.btnUseVoucher.setText("Không áp dụng được");
                statusText = "❌ Không thể áp dụng";
                statusColor = R.color.primary_red;
            }

            holder.btnUseVoucher.setBackgroundResource(R.drawable.bg_voucher_disabled);
            holder.tvStatus.setText(statusText);
            holder.tvStatus.setTextColor(context.getResources().getColor(statusColor));
        }
    }

    private double calculateDiscountForOrder(Voucher voucher, double orderTotal) {
        if (orderTotal < voucher.getMinOrderValue()) {
            return 0;
        }

        double discount = 0;
        if ("percent".equals(voucher.getDiscountType()) || "percentage".equals(voucher.getDiscountType())) {
            discount = orderTotal * (voucher.getDiscountValue() / 100);
            // Apply maximum discount if specified (you may need to add this field to Voucher model)
            // if (voucher.getMaxDiscountAmount() > 0 && discount > voucher.getMaxDiscountAmount()) {
            //     discount = voucher.getMaxDiscountAmount();
            // }
        } else {
            discount = voucher.getDiscountValue();
        }

        // Ensure discount doesn't exceed order total
        return Math.min(discount, orderTotal);
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    private String getDiscountText(Voucher voucher) {
        if ("percent".equals(voucher.getDiscountType()) || "percentage".equals(voucher.getDiscountType())) {
            return String.format("Giảm %d%%", (int) voucher.getDiscountValue());
        } else {
            return String.format("Giảm %s", formatCurrency(voucher.getDiscountValue()));
        }
    }

    private String getDescriptionText(Voucher voucher) {
        StringBuilder description = new StringBuilder();

        // Add base description if available
        if (voucher.getDescription() != null && !voucher.getDescription().trim().isEmpty()) {
            description.append(voucher.getDescription());
        }

        // Add minimum order condition - làm rõ là "từ" không phải "đúng"
        if (voucher.getMinOrderValue() >= 0) {
            if (description.length() >= 0) {
                description.append(" • ");
            }
            description.append("Áp dụng cho đơn từ ").append(formatCurrency(voucher.getMinOrderValue()));
        }

        // Add usage limit info
        int remainingUsage = voucher.getUsageLimit() - voucher.getUsedCount();
        if (remainingUsage <= 10) { // Show urgency for low stock
            if (description.length() > 0) {
                description.append(" • ");
            }
            description.append("Chỉ còn ").append(remainingUsage).append(" lượt");
        }

        return description.length() >= 0 ? description.toString() : "Không có điều kiện đặc biệt";
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(amount) + "₫";
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