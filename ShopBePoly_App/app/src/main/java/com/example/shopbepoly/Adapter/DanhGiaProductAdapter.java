package com.example.shopbepoly.Adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.DTO.ProductInOrder;
import com.example.shopbepoly.DTO.Review;
import com.example.shopbepoly.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DanhGiaProductAdapter extends RecyclerView.Adapter<DanhGiaProductAdapter.ViewHolder> {

    private Context context;
    private List<ProductInOrder> productList;

    // Lưu rating và comment tương ứng từng sản phẩm
    private ArrayList<Integer> ratings;
    private ArrayList<String> comments;

    public DanhGiaProductAdapter(Context context, List<ProductInOrder> productList) {
        this.context = context;
        this.productList = productList;
        this.ratings = new ArrayList<>(Collections.nCopies(productList.size(), 5)); // mặc định 5 sao
        this.comments = new ArrayList<>(Collections.nCopies(productList.size(), ""));
    }

    /**
     * Lấy tất cả review đã nhập để gửi API
     */
    public List<Review> getAllReviews(String userId) {
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < productList.size(); i++) {
            ProductInOrder p = productList.get(i);
            reviews.add(new Review(
                    userId,
                    p.getId_product().get_id(),
                    p.getOrderId(),
                    ratings.get(i),
                    comments.get(i),
                    new ArrayList<>() // chưa có ảnh
            ));
        }
        return reviews;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName;
        ImageView star1, star2, star3, star4, star5;
        EditText etComment;
        ImageView[] starViews;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            etComment = itemView.findViewById(R.id.edt_comment);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
            starViews = new ImageView[]{star1, star2, star3, star4, star5};
        }

        void updateStars(int rating) {
            for (int i = 0; i < starViews.length; i++) {
                starViews[i].setImageResource(i < rating ? R.drawable.star_filled : R.drawable.star);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_danhgia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductInOrder productInOrder = productList.get(position);

        // Hiển thị tên sản phẩm
        if (productInOrder.getId_product() != null &&
                productInOrder.getId_product().getNameproduct() != null &&
                !productInOrder.getId_product().getNameproduct().isEmpty()) {
            holder.tvName.setText(productInOrder.getId_product().getNameproduct());
        } else {
            holder.tvName.setText("Sản phẩm");
        }

        // Lấy ảnh theo màu đã chọn
        String selectedColor = productInOrder.getColor() != null ? productInOrder.getColor() : "";
        String imgUrl = productInOrder.getImg(selectedColor);
        if (imgUrl != null && !imgUrl.startsWith("http")) {
            imgUrl = ApiClient.IMAGE_URL + imgUrl;
        }

        Glide.with(context)
                .load(imgUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgProduct);

        // Set rating hiện tại
        holder.updateStars(ratings.get(position));

        // Xử lý click chọn sao
        for (int i = 0; i < holder.starViews.length; i++) {
            final int starIndex = i;
            holder.starViews[i].setOnClickListener(v -> {
                ratings.set(position, starIndex + 1);
                holder.updateStars(starIndex + 1);
            });
        }

        // Tránh vòng lặp TextWatcher cũ khi recycle view
        holder.etComment.removeTextChangedListener((TextWatcher) holder.etComment.getTag());

        // Set comment hiện tại
        holder.etComment.setText(comments.get(position));

        // Lắng nghe thay đổi comment
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                comments.set(position, s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        holder.etComment.addTextChangedListener(watcher);
        holder.etComment.setTag(watcher);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }
}