package com.example.shopbepoly.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.DTO.Category;
import com.example.shopbepoly.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList = new ArrayList<>();
    private Context context;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
        void onAllCategoryClick();
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }
    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }
    public CategoryAdapter(Context context) {
        this.context = context;
    }


    public void setCategoryList(List<Category> categories) {
        this.categoryList = categories;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {

        if (position == 0) {
            //item dau la All
            holder.tvTitle.setText("All");
            holder.imgCategory.setImageResource(R.drawable.ic_all_categories);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null){
                    listener.onAllCategoryClick();
                }
            });
        } else {
            Category category = categoryList.get(position - 1);
            holder.tvTitle.setText(category.getTitle());

            Glide.with(context)
                    .load(ApiClient.IMAGE_URL + category.getCateImg())
                    .into(holder.imgCategory);


            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }

        //        Category category = categoryList.get(position);
//        holder.tvTitle.setText(category.getTitle());
//
//        Glide.with(context)
//                .load(ApiClient.IMAGE_URL + category.getCateImg())
//                .into(holder.imgCategory);
//
//
//        holder.itemView.setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onCategoryClick(category);
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return categoryList.size() + 1;
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCategory;
        TextView tvTitle;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvTitle = itemView.findViewById(R.id.tvCategoryTitle);
        }
    }
}
