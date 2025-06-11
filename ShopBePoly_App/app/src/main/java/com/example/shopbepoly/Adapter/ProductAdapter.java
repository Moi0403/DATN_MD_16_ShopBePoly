package com.example.shopbepoly.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.ChiTietSanPham;
import com.example.shopbepoly.fragment.FavoriteFragment;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;


    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvProductName.setText(product.getNameproduct());

        // Sử dụng method getFormattedPrice() đã có sẵn trong model
        holder.tvProductPrice.setText(product.getFormattedPrice());

        holder.tvProductSold.setText("Đã bán: " + product.getSold() + " sp");

        Picasso.get().load(ApiClient.IMAGE_URL + product.getAvt_imgproduct()).into(holder.ivProductImage);
        updateFavoriteIcon(holder.imgFavorite,product);
        holder.ivProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChiTietSanPham.class);
                intent.putExtra("product",product);
                context.startActivity(intent);
            }
        });
        holder.imgFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FavoriteFragment.isFavorite(product)) {
                    FavoriteFragment.remove(product);
                } else {
                    FavoriteFragment.add(product);
                }
                updateFavoriteIcon(holder.imgFavorite, product);
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

    }
    {   // khối init
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return productList.get(position).get_id().hashCode();
    }
    private void updateFavoriteIcon(ImageView imgView, Product product) {
        if (FavoriteFragment.isFavorite(product)) {
            imgView.setImageResource(R.drawable.ic_heart_filled);
        } else {
            imgView.setImageResource(R.drawable.ic_heart_outline);
        }
    }
    public void setData(List<Product> newList) {
        this.productList.clear();
        this.productList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage,imgFavorite;
        TextView tvProductName;
        TextView tvProductPrice;
        TextView tvProductSold;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductSold = itemView.findViewById(R.id.tvProductSold);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);

        }
    }
}
