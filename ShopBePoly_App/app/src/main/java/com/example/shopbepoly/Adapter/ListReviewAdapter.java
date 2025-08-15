package com.example.shopbepoly.Adapter;

import android.content.Context;
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
import com.example.shopbepoly.DTO.ListReview;
import com.example.shopbepoly.R;

import java.util.ArrayList;
import java.util.List;

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

        // ✅ Hiển thị tên user
        holder.tvName.setText(getDisplayName(review));

        // ✅ Hiển thị avatar user
        String avatarUrl = getAvatarUrl(review);
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
        float ratingValue = review.getRating() > 5 ? 5f : Math.max(review.getRating(), 0f);
        holder.ratingBar.setIsIndicator(true);
        holder.ratingBar.setStepSize(0.5f);
        holder.ratingBar.setNumStars(5);
        holder.ratingBar.setRating(ratingValue);

        // ✅ Hiển thị comment
        holder.tvComment.setText(review.getComment() != null ? review.getComment() : "");

        // ✅ Hiển thị ngày
        holder.tvDate.setText(formatIsoUtcToLocal(review.getCreatedAt()));

        // ✅ Chỉ chủ review mới được sửa
        String reviewUserId = getReviewUserId(review);
        boolean isMyReview = currentUserId != null && currentUserId.equals(reviewUserId);

        holder.btnEdit.setVisibility(isMyReview ? View.VISIBLE : View.GONE);
        if (isMyReview) {
            holder.btnEdit.setOnClickListener(v -> {
                if (editClickListener != null) editClickListener.onEditClick(review);
            });
        } else {
            holder.btnEdit.setOnClickListener(null);
        }
    }

    private String getDisplayName(ListReview r) {
        if (r.getUserId() != null) {
            if (r.getUserId().getName() != null && !r.getUserId().getName().isEmpty()) {
                return r.getUserId().getName();
            }
            if (r.getUserId().getUsername() != null && !r.getUserId().getUsername().isEmpty()) {
                return r.getUserId().getUsername();
            }
        }
        return "Người dùng";
    }

    private String getAvatarUrl(ListReview r) {
        if (r.getUserId() != null && r.getUserId().getAvatar() != null && !r.getUserId().getAvatar().isEmpty()) {
            String avtUrl = r.getUserId().getAvatar();
            if (!avtUrl.startsWith("http")) {
                avtUrl = "https://yourdomain.com/" + avtUrl; // ⚠️ thay bằng domain API thật của bạn
            }
            return avtUrl;
        }
        return null;
    }

    private String getReviewUserId(ListReview r) {
        if (r.getUserId() != null) {
            return r.getUserId().getId();
        }
        return null;
    }

    private String formatIsoUtcToLocal(String isoUtc) {
        if (isoUtc == null || isoUtc.isEmpty()) return "";
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", java.util.Locale.US);
            in.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date d = in.parse(isoUtc);

            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            out.setTimeZone(java.util.TimeZone.getDefault());
            return out.format(d);
        } catch (Exception e) {
            return isoUtc;
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