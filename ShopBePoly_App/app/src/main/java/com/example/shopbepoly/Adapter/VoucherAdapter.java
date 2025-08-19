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
import com.example.shopbepoly.API.ApiService.VoucherUsageResponse;
import com.example.shopbepoly.DTO.Voucher;
import com.example.shopbepoly.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    private Context context;
    private List<Voucher> voucherList;
    private OnVoucherClickListener listener;
    private SharedPreferences sharedPreferences;
    private ApiService apiService;
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
        this.apiService = ApiClient.getApiService();

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
                description = "Cho đơn hàng từ " + formatPrice(voucher.getMinOrderValue());
            } else {
                description = voucher.getDescription();
            }
            tvVoucherDescription.setText(description);

            // Set voucher code
            tvVoucherCode.setText(voucher.getCode());

            // Set start and end date with time
            String dateTimeRange = formatDateTimeRange(voucher.getStartDate(), voucher.getEndDate());
            tvVoucherExpiry.setText(dateTimeRange);

            // Check if voucher is saved for current user
            boolean isSaved = isVoucherSavedForCurrentUser(voucher.getId());

            // Kiểm tra xem voucher còn lượt sử dụng không
            boolean hasRemainingUsage = voucher.getUsedCount() < voucher.getUsageLimit();

            if (!hasRemainingUsage) {
                // Voucher đã hết lượt sử dụng
                btnSaveVoucher.setText("Đã hết");
                btnSaveVoucher.setBackgroundResource(R.drawable.disabled_button_bg);
                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.gray));
                btnSaveVoucher.setEnabled(false);
            } else if (isSaved) {
                btnSaveVoucher.setText("Đã lưu");
                btnSaveVoucher.setBackgroundResource(R.drawable.saved_button_bg);
                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.gray));
                btnSaveVoucher.setEnabled(true);
            } else {
                btnSaveVoucher.setText("Lưu");
                btnSaveVoucher.setBackgroundResource(R.drawable.save_button_bg);
                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.white));
                btnSaveVoucher.setEnabled(true);
            }

            // Handle save button click
            btnSaveVoucher.setOnClickListener(v -> {
                if (currentUserId.isEmpty()) {
                    Toast.makeText(context, "Vui lòng đăng nhập để lưu voucher!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!hasRemainingUsage) {
                    Toast.makeText(context, "Mã giảm giá đã hết lượt sử dụng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isSaved) {
                    // Gọi API để sử dụng voucher trước khi lưu
                    useVoucherOnServer(voucher, new VoucherUsageCallback() {
                        @Override
                        public void onSuccess(Voucher updatedVoucher) {
                            // Cập nhật voucher trong list
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION && position < voucherList.size()) {
                                voucherList.set(position, updatedVoucher);
                            }

                            // Lưu voucher cho user hiện tại
                            saveVoucherForCurrentUser(updatedVoucher.getId());

                            // Cập nhật UI
                            btnSaveVoucher.setText("Đã lưu");
                            btnSaveVoucher.setBackgroundResource(R.drawable.saved_button_bg);
                            btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.gray));

                            Toast.makeText(context, "Đã lưu mã giảm giá!", Toast.LENGTH_SHORT).show();

                            // Gọi callback nếu có
                            if (listener != null) {
                                listener.onSaveVoucherClick(updatedVoucher);
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            if (errorMessage.contains("hết") || errorMessage.contains("limit")) {
                                // Voucher đã hết lượt sử dụng
                                btnSaveVoucher.setText("Đã hết");
                                btnSaveVoucher.setBackgroundResource(R.drawable.disabled_button_bg);
                                btnSaveVoucher.setTextColor(context.getResources().getColor(R.color.gray));
                                btnSaveVoucher.setEnabled(false);

                                Toast.makeText(context, "Mã giảm giá đã hết lượt sử dụng!", Toast.LENGTH_SHORT).show();

                                if (listener != null) {
                                    listener.onVoucherUsageLimitReached(voucher);
                                }
                            } else {
                                Toast.makeText(context, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
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

        // Check voucher đã lưu cho user hiện tại chưa
        private boolean isVoucherSavedForCurrentUser(String voucherId) {
            if (currentUserId.isEmpty()) {
                return false;
            }

            String key = "saved_vouchers_" + currentUserId;
            Set<String> savedVouchers = sharedPreferences.getStringSet(key, new HashSet<>());
            return savedVouchers.contains(voucherId);
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

        private void useVoucherOnServer(Voucher voucher, VoucherUsageCallback callback) {
            Call<VoucherUsageResponse> call = apiService.useVoucher(voucher.getId());
            call.enqueue(new Callback<VoucherUsageResponse>() {
                @Override
                public void onResponse(Call<VoucherUsageResponse> call, Response<VoucherUsageResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        VoucherUsageResponse voucherResponse = response.body();
                        if (voucherResponse.isSuccess()) {
                            callback.onSuccess(voucherResponse.getVoucher());
                        } else {
                            callback.onError(voucherResponse.getMessage());
                        }
                    } else {
                        callback.onError("Không thể kết nối đến server");
                    }
                }

                @Override
                public void onFailure(Call<VoucherUsageResponse> call, Throwable t) {
                    callback.onError("Lỗi kết nối: " + t.getMessage());
                }
            });
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

    // Callback interface cho việc sử dụng voucher
    private interface VoucherUsageCallback {
        void onSuccess(Voucher updatedVoucher);
        void onError(String errorMessage);
    }
}