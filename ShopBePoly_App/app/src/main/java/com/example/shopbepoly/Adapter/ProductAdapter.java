package com.example.shopbepoly.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Favorite;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.ChiTietSanPham;
import com.example.shopbepoly.fragment.CartBottomSheetDialog;
import com.example.shopbepoly.fragment.FavoriteFragment;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;



    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        setHasStableIds(true);
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

            Glide.with(context)
                .load(ApiClient.IMAGE_URL + product.getAvt_imgproduct())
                .placeholder(R.drawable.ic_launcher_background) // thêm ảnh chờ
                .error(R.drawable.ic_launcher_foreground) // thêm ảnh lỗi
                .override(300, 300) // giảm độ phân giải để nhẹ
                .centerCrop()
                .into(holder.ivProductImage);


        updateFavoriteIcon(holder.imgFavorite,product);
        holder.ivProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChiTietSanPham.class);
                intent.putExtra("product",product);
                context.startActivity(intent);
            }
        });
        holder.imgFavorite.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Product p = productList.get(pos);
            SharedPreferences prefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String userId = prefs.getString("userId", null);

            if (userId == null) {
                Toast.makeText(context, "Bạn cần đăng nhập để yêu thích", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService api = ApiClient.getApiService();
            boolean wasFavorite = FavoriteFragment.isFavorite(p);

            if (wasFavorite) {
                // UI cập nhật trước
                FavoriteFragment.remove(context,p);
                updateFavoriteIcon(holder.imgFavorite, p);
                notifyItemChanged(pos);
                Toast.makeText(context, "Đã xoá khỏi yêu thích", Toast.LENGTH_SHORT).show();


                api.removeFavorite(userId,p.get_id()).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            FavoriteFragment.add(context,p);
                            updateFavoriteIcon(holder.imgFavorite, p);
                            notifyItemChanged(pos);
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(context, "Xoá yêu thích thất bại", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        FavoriteFragment.add(context,p);
                        notifyItemChanged(pos);
                        Toast.makeText(context, "Lỗi kết nối khi xoá yêu thích", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                // UI cập nhật trước
                FavoriteFragment.add(context,p);
                notifyItemChanged(pos);

                Favorite fav = new Favorite(userId, product.get_id());
                api.addFavorite(fav).enqueue(new Callback<Favorite>() {
                    @Override
                    public void onResponse(Call<Favorite> call, Response<Favorite> response) {
                        if (response.isSuccessful()) {
                            // ✅ Thêm thành công → hiển thị toast trên main thread
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show()
                            );
                        } else {
                            try {
                                String errorMsg = response.errorBody().string();
                                if (errorMsg.contains("tồn tại")) {
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            Toast.makeText(context, "Sản phẩm đã có trong yêu thích", Toast.LENGTH_SHORT).show()

                                    );
                                    return;
                                } else {
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            Toast.makeText(context, "Thêm yêu thích thất bại", Toast.LENGTH_SHORT).show()
                                    );
                                }
                            } catch (Exception e) {
                                new Handler(Looper.getMainLooper()).post(() ->
                                        Toast.makeText(context, "Lỗi không xác định", Toast.LENGTH_SHORT).show()
                                );
                            }

                            FavoriteFragment.remove(context,p);
                            notifyItemChanged(pos);
                        }
                    }

                    @Override
                    public void onFailure(Call<Favorite> call, Throwable t) {
                        FavoriteFragment.remove(context,p);
                        notifyItemChanged(pos);

                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "Lỗi kết nối khi thêm yêu thích", Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            }
        });


        holder.ivCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                // Lấy userId từ SharedPreferences
//                SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
//                String userId = sharedPreferences.getString("userId", null);
//
//                if (userId == null) {
//                    Toast.makeText(context, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                Cart cart = new Cart();
//                cart.setIdProduct(product);
//                cart.setIdUser(userId); // Gán userId lấy từ SharedPreferences
//                cart.setPrice(product.getPrice());
//                cart.setQuantity(1);
//                cart.setTotal(product.getPrice() * 1);
//                cart.setStatus(0);
//                Add_Cart(cart);
                CartBottomSheetDialog dialog = new CartBottomSheetDialog(context, product);
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "CartBottomSheetDialog");
            }
        });

    }
    {   // khối init

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
        if (newList == null) return;
        this.productList.clear();
        this.productList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage,imgFavorite, ivCart;
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
            ivCart = itemView.findViewById(R.id.ivCart);
        }
    }

    private void Add_Cart(Cart cart){
        ApiService apiService = ApiClient.getApiService();
        Call<Cart> call = apiService.addCart(cart);
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Thêm giỏ hàng thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("AddCartError", "Response code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(context, "Thêm giỏ hàng không thành công", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Log.e("AddCartError", t.getMessage(), t);
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
