package com.example.shopbepoly.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.R;

import java.util.List;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.ProductViewHolder> {
    private Context context;
    private List<String> productNames;
    private List<String> imageUrls;

    public OrderProductAdapter(Context context, List<String> names, List<String> images) {
        this.context = context;
        this.productNames = names;
        this.imageUrls = images;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_order, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.txtProductName.setText(productNames.get(position));
        String img = imageUrls.size() > position ? imageUrls.get(position) : "";
        if (!img.startsWith("http")) {
            img = ApiClient.IMAGE_URL + img;
        }
        Glide.with(context).load(img)
                .placeholder(R.drawable.avatar_default)
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return productNames.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
        }
    }
}