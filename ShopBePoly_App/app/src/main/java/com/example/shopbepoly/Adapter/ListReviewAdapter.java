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

import com.example.shopbepoly.DTO.ListReview;
import com.example.shopbepoly.R;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        // Debug xem userId có dữ liệu gì
        Log.d("ListReview", "Review data: " + new Gson().toJson(review));

// Set tên người dùng
        if (review.getUserId() != null && review.getUserId().getUsername() != null) {
            holder.tvName.setText(review.getUserId().getUsername());
        } else {
            holder.tvName.setText("Người dùng");
        }

// Rating
        float ratingValue = 0f;
        try {
            ratingValue = (float) review.getRating();
            if (ratingValue < 0f) ratingValue = 0f;
            if (ratingValue > 5f) ratingValue = 5f;
        } catch (Exception ignored) {}
        holder.ratingBar.setRating(ratingValue); // hoặc 6 - ratingValue nếu API ngược

        holder.tvComment.setText(review.getComment() != null ? review.getComment() : "");

        // Ngày tạo
        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat =
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String formattedDate = outputFormat.format(inputFormat.parse(review.getCreatedAt()));
            holder.tvDate.setText(formattedDate);
        } catch (Exception e) {
            holder.tvDate.setText(review.getCreatedAt() != null ? review.getCreatedAt() : "");
        }

        // Nút sửa — chỉ hiện nếu review thuộc về user hiện tại
        boolean isMyReview = false;
        if (currentUserId != null && review.getUserId() != null) {
            String reviewUserId = review.getUserId().getId();
            if (reviewUserId != null && reviewUserId.equals(currentUserId)) {
                isMyReview = true;
            }
        }

        holder.btnEdit.setVisibility(isMyReview ? View.VISIBLE : View.GONE);

        if (isMyReview) {
            holder.btnEdit.setOnClickListener(v -> {
                if (editClickListener != null) {
                    editClickListener.onEditClick(review);
                }
            });
        } else {
            holder.btnEdit.setOnClickListener(null);
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