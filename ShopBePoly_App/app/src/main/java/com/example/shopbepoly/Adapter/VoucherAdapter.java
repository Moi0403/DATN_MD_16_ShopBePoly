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

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
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
    private String currentUserId;

    public interface OnVoucherClickListener {
        void onVoucherClick(Voucher voucher);
        void onSaveVoucherClick(Voucher voucher);
        void onVoucherUsageLimitReached(Voucher voucher);
    }

    public VoucherAdapter(Context context, List<Voucher> voucherList) {
        this.context = context;
        this.voucherList = voucherList;
        this.sharedPreferences = context.getSharedPreferences("VoucherPrefs", Context.MODE_PRIVATE);

        // Lấy user ID hiện tại
        SharedPreferences userPrefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        this.currentUserId = userPrefs.getString("userId", "");
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
            if ("percent".equals(voucher.getDiscountType()) || "percentage".equals(voucher.getDiscountType())) {
                tvVoucherTitle.setText("Giảm " + (int)voucher.getDiscountValue() + "%");
            } else {
                tvVoucherTitle.setText("Giảm " + formatPrice(voucher.getDiscountValue()));
            }

            // Set description
            String description = "";
            if (voucher.getMinOrderValue() > 0) {
                description = "Đơn tối thiểu " + formatPrice(voucher.getMinOrderValue());
            } else {
                description = voucher.getDescription();
            }
            tvVoucherDescription.setText(description);

            // Set voucher code
            tvVoucherCode.setText(voucher.getCode());

            // Set start and end date with time
            String dateTimeRange = formatDateTimeRange(voucher.getStartDate(), voucher.getEndDate());
            tvVoucherExpiry.setText(dateTimeRange);

            // Check voucher status
            boolean isSaved = isVoucherSavedForCurrentUser(voucher.getId());
            boolean isUsed = isVoucherUsedByCurrentUser(voucher.getId());
            boolean hasRemainingUsage = voucher.getUsedCount() < voucher.getUsageLimit();
            boolean isExpired = voucher.isExpired();
            boolean isActive = voucher.isActive();

            // Update button state based on voucher status
            updateButtonState(voucher, isSaved, isUsed, hasRemainingUsage, isExpired, isActive);

            // Handle save button click
            btnSaveVoucher.setOnClickListener(v -> handleSaveButtonClick(voucher, isSaved, isUsed, hasRemainingUsage, isExpired, isActive));

            // Handle item click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVoucherClick(voucher);
                }
            });
        }

        // Method cập nhật trạng thái button
        private void updateButtonState(Voucher voucher, boolean isSaved, boolean isUsed, boolean hasRemainingUsage, boolean isExpired, boolean isActive) {
            if (isUsed) {
                // Voucher đã được sử dụng bởi user này
                btnSaveVoucher.setText("Đã sử dụng");
                btnSaveVoucher.setBackgroundResource(R.drawable.used_button_bg);
                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.primary_red));
                btnSaveVoucher.setEnabled(false);

                // Làm mờ toàn bộ item
                itemView.setAlpha(0.6f);
            } else if (!isActive || isExpired) {
                // Voucher không hoạt động hoặc đã hết hạn
                btnSaveVoucher.setText("Đã hết hạn");
                btnSaveVoucher.setBackgroundResource(R.drawable.disabled_button_bg);
                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.gray));
                btnSaveVoucher.setEnabled(false);
                itemView.setAlpha(0.6f);
            } else if (!hasRemainingUsage) {
                // Voucher đã hết lượt sử dụng tổng thể
                btnSaveVoucher.setText("Đã hết");
                btnSaveVoucher.setBackgroundResource(R.drawable.disabled_button_bg);
                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.gray));
                btnSaveVoucher.setEnabled(false);
                itemView.setAlpha(0.6f);
            } else if (isSaved) {
                // Voucher đã được lưu nhưng chưa sử dụng
                btnSaveVoucher.setText("Đã lưu");
                btnSaveVoucher.setBackgroundResource(R.drawable.saved_button_bg);
                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.primary_color));
                btnSaveVoucher.setEnabled(true);
                itemView.setAlpha(1.0f);
            } else {
                // Voucher có thể lưu
                btnSaveVoucher.setText("Lưu");
                btnSaveVoucher.setBackgroundResource(R.drawable.save_button_bg);
                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.white));
                btnSaveVoucher.setEnabled(true);
                itemView.setAlpha(1.0f);
            }
        }

        // Method xử lý click button lưu
        private void handleSaveButtonClick(Voucher voucher, boolean isSaved, boolean isUsed, boolean hasRemainingUsage, boolean isExpired, boolean isActive) {
            if (currentUserId.isEmpty()) {
                Toast.makeText(context, "Vui lòng đăng nhập để lưu voucher!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isUsed) {
                Toast.makeText(context, "Bạn đã sử dụng mã giảm giá này rồi!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isActive || isExpired) {
                Toast.makeText(context, "Mã giảm giá đã hết hạn!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!hasRemainingUsage) {
                Toast.makeText(context, "Mã giảm giá đã hết lượt sử dụng!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isSaved) {
                // Lưu voucher mà không cần gọi API useVoucher
                saveVoucherForCurrentUser(voucher.getId());

                // Cập nhật UI
                btnSaveVoucher.setText("Đã lưu");
                btnSaveVoucher.setBackgroundResource(R.drawable.saved_button_bg);
                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.primary_color));

                Toast.makeText(context, "Đã lưu mã giảm giá!", Toast.LENGTH_SHORT).show();

                // Gọi callback nếu có
                if (listener != null) {
                    listener.onSaveVoucherClick(voucher);
                }
            } else {
                Toast.makeText(context, "Mã đã được lưu trước đó", Toast.LENGTH_SHORT).show();
            }
        }

        // Check voucher đã lưu cho user hiện tại chưa
        private boolean isVoucherSavedForCurrentUser(String voucherId) {
            if (currentUserId.isEmpty()) {
                return false;
            }

            String key = "saved_vouchers_" + currentUserId;
            Set<String> savedVouchers = sharedPreferences.getStringSet(key, new HashSet<>());
            return savedVouchers.contains(voucherId);
        }

        // Check voucher đã được sử dụng bởi user hiện tại chưa
        private boolean isVoucherUsedByCurrentUser(String voucherId) {
            if (currentUserId.isEmpty()) {
                return false;
            }

            String key = "used_vouchers_" + currentUserId;
            Set<String> usedVouchers = sharedPreferences.getStringSet(key, new HashSet<>());
            return usedVouchers.contains(voucherId);
        }

        // Lưu voucher cho user hiện tại
        private void saveVoucherForCurrentUser(String voucherId) {
            if (currentUserId.isEmpty()) {
                return;
            }

            String key = "saved_vouchers_" + currentUserId;
            Set<String> savedVouchers = sharedPreferences.getStringSet(key, new HashSet<>());
            savedVouchers = new HashSet<>(savedVouchers); // Create new set to avoid modification issues
            savedVouchers.add(voucherId);
            sharedPreferences.edit().putStringSet(key, savedVouchers).apply();
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

        private String formatDateTimeRange(Date startDate, Date endDate) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            if (startDate == null || endDate == null) {
                return "Thời gian không xác định";
            }

            String startDateStr = dateFormat.format(startDate);
            String endDateStr = dateFormat.format(endDate);
            String startTimeStr = timeFormat.format(startDate);
            String endTimeStr = timeFormat.format(endDate);

            if (startDateStr.equals(endDateStr)) {
                return String.format("%s (%s - %s)", startDateStr, startTimeStr, endTimeStr);
            } else {
                return String.format("Từ %s đến %s",
                        dateTimeFormat.format(startDate),
                        dateTimeFormat.format(endDate));
            }
        }
    }
}