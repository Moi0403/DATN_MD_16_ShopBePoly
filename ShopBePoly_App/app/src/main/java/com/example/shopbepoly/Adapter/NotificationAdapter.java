package com.example.shopbepoly.Adapter;

import static com.example.shopbepoly.API.ApiClient.IMAGE_URL;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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


    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
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

        if (n.getProducts() != null && !n.getProducts().isEmpty()) {
            Notification.ProductInfo p = n.getProducts().get(0);
//            holder.txtProductName.setText("Sản phẩm: " + (p.getProductName() != null ? p.getProductName() : ""));

            // Load ảnh sản phẩm nếu có
            if (p.getImg() != null && !p.getImg().isEmpty()) {
                Glide.with(context)
                        .load(IMAGE_URL + p.getImg())
                        .placeholder(R.drawable.niker) // Ảnh mặc định khi chưa load xong
                        .error(R.drawable.avatar_default)       // Ảnh lỗi
                        .into(holder.imgNofi);
            } else {
                holder.imgNofi.setImageResource(R.drawable.niker);
            }
        } else {
//            holder.txtProductName.setText("");
            holder.imgNofi.setImageResource(R.drawable.niker);
        }

        if (n.getCreatedAt() != null) {
            holder.txtTime.setText(formatDate(n.getCreatedAt()));
        } else {
            holder.txtTime.setText("Không rõ thời gian");
        }
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
//            txtProductName = itemView.findViewById(R.id.txtName_product_notifi);
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
}
