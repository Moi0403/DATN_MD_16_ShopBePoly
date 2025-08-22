package com.example.shopbepoly.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.DTO.ListReview;
import com.example.shopbepoly.DTO.User;
import com.example.shopbepoly.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ListReviewAdapter extends RecyclerView.Adapter<ListReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private List<ListReview> reviewList = new ArrayList<>();
    private final OnEditClickListener editClickListener;
    private String currentUserId; // để kiểm tra quyền sửa

    public interface OnEditClickListener {
        void onEditClick(ListReview review);
    }

    public ListReviewAdapter(Context context, OnEditClickListener listener) {
        this.context = context;
        this.editClickListener = listener;
    }

    public void setReviews(List<ListReview> reviews) {
        this.reviewList = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ListReview review = reviewList.get(position);
        if (review == null) return;

        User user = review.getUser();
        if (user != null) {
            Log.d("ListReviewAdapter", "UserId: " + user.getId()
                    + " | Name: " + user.getName()
                    + " | Avatar: " + user.getAvatar());
        } else {
            Log.d("ListReviewAdapter", "User NULL for reviewId=" + review.getId());
        }

        // ✅ Hiển thị tên user
        holder.tvName.setText(getDisplayName(user));

        // ✅ Hiển thị avatar user
        String avatarUrl = getAvatarUrl(user);
        if (avatarUrl != null) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.person);
        }

        // ✅ Hiển thị rating
        float ratingValue = review.getRating();
        if (ratingValue < 0) ratingValue = 0f;
        if (ratingValue > 5) ratingValue = 5f;

        holder.ratingBar.setIsIndicator(true);
        holder.ratingBar.setStepSize(0.5f);
        holder.ratingBar.setNumStars(5);
        holder.ratingBar.setRating(ratingValue);

        // ✅ Hiển thị comment
        holder.tvComment.setText(review.getComment() != null ? review.getComment() : "");

        // ✅ Hiển thị ngày
        holder.tvDate.setText(formatIsoUtcToLocal(review.getCreatedAt()));

        // ✅ Chỉ chủ review mới được sửa
        boolean isMyReview = currentUserId != null && currentUserId.equals(review.getUserId());
        holder.btnEdit.setVisibility(isMyReview ? View.VISIBLE : View.GONE);
        holder.btnEdit.setOnClickListener(isMyReview ? v -> {
            if (editClickListener != null) editClickListener.onEditClick(review);
        } : null);
    }

    private String getDisplayName(User u) {
        if (u != null) {
            if (u.getName() != null && !u.getName().isEmpty()) return u.getName();
        }
        return "Người dùng";
    }

    private String getAvatarUrl(User u) {
        if (u != null) {
            String avatarFile = u.getAvatar();
            Log.d("ListReviewAdapter", "Avatar raw = " + avatarFile);
            if (avatarFile != null && !avatarFile.isEmpty()) {
                // Nếu API trả full URL thì dùng trực tiếp
                if (avatarFile.startsWith("http")) {
                    return avatarFile;
                }
                // Nếu chỉ trả fileName thì nối với base URL
                return ApiClient.IMAGE_URL + avatarFile + "?t=" + System.currentTimeMillis();
            }
        }
        return null;
    }

    private String formatIsoUtcToLocal(String isoUtc) {
        if (isoUtc == null || isoUtc.isEmpty()) return "";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);
            in.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = in.parse(isoUtc);

            SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            out.setTimeZone(TimeZone.getDefault());
            return out.format(d);
        } catch (Exception e) {
            return isoUtc; // fallback
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvDate, tvComment;
        RatingBar ratingBar;
        Button btnEdit;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvComment = itemView.findViewById(R.id.tv_comment);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }
    }
}