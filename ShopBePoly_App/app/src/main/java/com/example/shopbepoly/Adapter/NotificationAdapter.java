package com.example.shopbepoly.Adapter;

import static com.example.shopbepoly.API.ApiClient.IMAGE_URL;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.Chitietdonhang;
import com.example.shopbepoly.DTO.Notification;
import com.example.shopbepoly.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private OnNotificationClickListener clickListener;

    // Interface để callback khi click item
    public interface OnNotificationClickListener {
        void onNotificationRead(String notificationId);
    }

    public NotificationAdapter(Context context, List<Notification> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.clickListener = listener;
    }

    // Constructor cũ để backward compatibility
    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        this.clickListener = null;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification n = notificationList.get(position);
        if (n == null) return;

        holder.txtTitle.setText(n.getTitle() != null ? n.getTitle() : "");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.txtContent.setText(Html.fromHtml(n.getContent(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.txtContent.setText(Html.fromHtml(n.getContent()));
        }

        // Hiển thị trạng thái đã đọc/chưa đọc
        if (n.isRead()) {
            // Đã đọc - màu nền trắng, text xám
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
//            holder.txtTitle.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
//            holder.txtContent.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        } else {
            // Chưa đọc - màu nền xanh nhạt, text đen
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_blue));
            holder.txtTitle.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            holder.txtContent.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }

        // Load ảnh sản phẩm
        if (n.getProducts() != null && !n.getProducts().isEmpty()) {
            Notification.ProductInfo p = n.getProducts().get(0);
            if (p.getImg() != null && !p.getImg().isEmpty()) {
                Glide.with(context)
                        .load(IMAGE_URL + p.getImg())
                        .placeholder(R.drawable.niker)
                        .error(R.drawable.avatar_default)
                        .into(holder.imgNofi);
            } else {
                holder.imgNofi.setImageResource(R.drawable.niker);
            }
        } else {
            holder.imgNofi.setImageResource(R.drawable.niker);
        }

        // Hiển thị thời gian
        if (n.getCreatedAt() != null) {
            holder.txtTime.setText(formatDate(n.getCreatedAt()));
        } else {
            holder.txtTime.setText("Không rõ thời gian");
        }

        // Xử lý click item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đánh dấu đã đọc nếu chưa đọc
                if (!n.isRead() && clickListener != null) {
                    clickListener.onNotificationRead(n.get_id());
                }

                // Navigate to detail page
                if (shouldNavigateToDetail(n.getType())) {
                    Intent intent = new Intent(context, Chitietdonhang.class);
                    intent.putExtra("orderId", n.getOrderId());
                    intent.putExtra("fromNotification", true);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList == null ? 0 : notificationList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtContent, txtTime;
        ImageView imgNofi;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txttitle_notifi);
            txtContent = itemView.findViewById(R.id.txtContent_notifi);
            txtTime = itemView.findViewById(R.id.txtTime);
            imgNofi = itemView.findViewById(R.id.imgNofi);
        }
    }

    private String formatDate(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(inputDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM - HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            return inputDate.length() >= 10 ? inputDate.substring(0, 10) : "Không rõ";
        }
    }

    private boolean shouldNavigateToDetail(String type) {
        return type != null && (
                type.equals("order") ||
                        type.equals("delivery") ||
                        type.equals("shipping") ||
                        type.equals("delivery_success") ||
                        type.equals("order_confirmed") ||
                        type.equals("order_cancelled")
        );
    }
}