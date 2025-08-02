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
import com.example.shopbepoly.DTO.ProductInOrder;
import com.example.shopbepoly.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

public class ProOrderAdapter extends RecyclerView.Adapter<ProOrderAdapter.ProOrderViewHolder> {
    private Context context;
    private List<ProductInOrder> list;

    public ProOrderAdapter(Context context, List<ProductInOrder> productList) {
        this.context = context;
        this.list = productList;
    }

    @NonNull
    @Override
    public ProOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_order, parent, false);
        return new ProOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProOrderViewHolder holder, int position) {
        ProductInOrder product = list.get(position);

        if (product.getId_product() != null) {
            holder.txtName.setText(product.getId_product().getNameproduct());

            String imageUrl = ApiClient.IMAGE_URL + product.getImg(product.getColor());

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .override(300, 300)
                    .centerCrop()
                    .into(holder.imgProduct);
        } else {
            holder.txtName.setText("Sản phẩm không tồn tại");
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.tvMau.setText(product.getColor() + ": ");
        holder.tvSize.setText(String.valueOf(product.getSize()));
        holder.txtQuantity.setText(String.valueOf(product.getQuantity()));
        holder.txtPrice.setText(formatCurrency(product.getPrice()));
    }


    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ProOrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, tvMau, tvSize, txtQuantity, txtPrice;

        public ProOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_pro_order);
            txtName = itemView.findViewById(R.id.txtName_order);
            tvMau = itemView.findViewById(R.id.tv_mau_order);
            tvSize = itemView.findViewById(R.id.text_size_order);
            txtQuantity = itemView.findViewById(R.id.txt_quantity_order);
            txtPrice = itemView.findViewById(R.id.txt_price_order);
        }
    }

    private String formatCurrency(int amount) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        return formatter.format(amount) + " VNĐ";
    }
}
