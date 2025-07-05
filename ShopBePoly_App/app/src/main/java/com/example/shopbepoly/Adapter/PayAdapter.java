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
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.R;

import java.util.List;

public class PayAdapter extends RecyclerView.Adapter<PayAdapter.PayViewHolder> {

    private Context context;
    private List<Cart> cartList;

    public PayAdapter(Context context, List<Cart> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public PayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pay_product, parent, false);
        return new PayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PayViewHolder holder, int position) {
        Cart cart = cartList.get(position);

        holder.txtProductName.setText(cart.getIdProduct().getNameproduct());
        holder.txtProductQuantity.setText("Số lượng: " + cart.getQuantity());
        holder.txtProductColor.setText(cart.getColor());
        holder.txtProductSize.setText("Size: " + cart.getSize());
        holder.txtProductPrice.setText(cart.getPrice() + " VND");

        Glide.with(context).load(cart.getImg_cart()).into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class PayViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName, txtProductQuantity, txtProductColor, txtProductSize, txtProductPrice;

        public PayViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductQuantity = itemView.findViewById(R.id.txtProductQuantity);
            txtProductColor = itemView.findViewById(R.id.txtProductColor);
            txtProductSize = itemView.findViewById(R.id.txtProductSize);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
        }
    }
}