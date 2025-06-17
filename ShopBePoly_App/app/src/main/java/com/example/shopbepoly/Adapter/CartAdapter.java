package com.example.shopbepoly.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.Variation;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.ChiTietSanPham;
import com.example.shopbepoly.fragment.CartFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<Cart> list_cart;
    private CartFragment frag_total;

    public CartAdapter(Context context, List<Cart> list, CartFragment fragment){
        this.context = context;
        this.list_cart = list;
        this.frag_total = fragment;
    }
    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Cart cart = list_cart.get(position);
        Product product = cart.getIdProduct();
        Log.d("CartAdapter", "Binding position: " + position);
        if (cart != null && cart.getIdProduct() != null) {
            Log.d("CartAdapter", "Product Name: " + product.getNameproduct() + ", Image: " + product.getAvt_imgproduct());
            Glide.with(context)
                    .load(ApiClient.IMAGE_URL + product.getAvt_imgproduct())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.imvAVT);
            holder.tvName.setText(product.getNameproduct() != null ? product.getNameproduct() : "N/A");
            holder.tvPrice.setText(cart.getTotal()+"");
            if (product.getVariations() != null && !product.getVariations().isEmpty()) {
                Variation variation = product.getVariations().get(0);
                holder.tvSize.setText(variation.getSize()+"");
            } else {
                holder.tvSize.setText("N/A");
            }
            holder.tvQuantity.setText(cart.getQuantity() > 0 ? String.valueOf(cart.getQuantity()) : "1");
        } else {
            Log.e("CartAdapter", "Cart hoặc Product null ở vị trí " + position);
        }

        holder.imv_tang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int slgHT = cart.getQuantity();
                cart.setQuantity(slgHT + 1);
                int gia = product.getPrice();
                cart.setTotal(gia * cart.getQuantity());
                holder.tvPrice.setText(cart.getTotal()+"");
                holder.tvQuantity.setText(cart.getQuantity()+"");
                update_quantity(cart);
                updateTotalPrice();
            }
        });

        holder.imv_giam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int slgHT = cart.getQuantity();
                if (slgHT > 1){
                    cart.setQuantity(slgHT - 1);
                    int gia = product.getPrice();
                    cart.setTotal(gia * cart.getQuantity());
                    holder.tvPrice.setText(cart.getTotal()+"");
                    holder.tvQuantity.setText(cart.getQuantity()+"");
                    update_quantity(cart);
                    updateTotalPrice();
                }
            }
        });

        holder.imv_xoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDE(position);
            }
        });

        updateTotalPrice();
        holder.cbk_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.cbk_add.isChecked()) {
                    cart.setStatus(1);
                } else {
                    cart.setStatus(0);
                }
                update_quantity(cart);
                updateTotalPrice();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChiTietSanPham.class);
                intent.putExtra("product",product);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list_cart.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder{
        ImageView imvAVT, imv_giam, imv_tang, imv_xoa;
        CheckBox cbk_add;
        TextView tvName, tvPrice, tvSize, tvQuantity;
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imvAVT = itemView.findViewById(R.id.image_product);
            imv_giam = itemView.findViewById(R.id.img_giam);
            imv_tang = itemView.findViewById(R.id.img_tang);
            imv_xoa = itemView.findViewById(R.id.img_delete);
            cbk_add = itemView.findViewById(R.id.checkbox_select);
            tvName = itemView.findViewById(R.id.text_name);
            tvPrice = itemView.findViewById(R.id.text_price);
            tvSize = itemView.findViewById(R.id.text_size);
            tvQuantity = itemView.findViewById(R.id.text_quantity);
        }
    }
    private void update_quantity(Cart cart){
        ApiService apiService = ApiClient.getApiService();
        Call<List<Cart>> call = apiService.upCart(cart.get_id(), cart);
        call.enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    Log.e("GioHang_ADT", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {

            }
        });
    }

    private void showDE(int position){
        Cart cart = list_cart.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.thongbao);
        builder.setTitle("Thông báo");
        builder.setMessage("Bạn có chắc chắn muốn xóa " + cart.getIdProduct().getNameproduct() + " không ?");
        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    ApiService apiService = ApiClient.getApiService();
                    Call<List<Cart>> call = apiService.delCart(cart.get_id());
                    call.enqueue(new Callback<List<Cart>>() {
                        @Override
                        public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                list_cart.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, list_cart.size());
                            } else {

                                Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Cart>> call, Throwable t) {

                        }
                    });

                } catch (Exception e){
                    Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }


    private void updateTotalPrice() {
        int total = 0;
        for (Cart item : list_cart) {
            if (item.getStatus() == 1) {
                total += item.getTotal();
            }
        }
        frag_total.tvTotal.setText(total + ".000");
    }
}
