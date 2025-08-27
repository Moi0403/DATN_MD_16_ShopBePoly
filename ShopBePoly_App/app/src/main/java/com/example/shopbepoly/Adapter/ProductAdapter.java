package com.example.shopbepoly.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import com.example.shopbepoly.DTO.RatingResponse;
import com.example.shopbepoly.DTO.Variation;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.ChiTietSanPham;
import com.example.shopbepoly.fragment.CartBottomSheetDialog;
import com.example.shopbepoly.fragment.FavoriteFragment;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

        int originalPrice = product.getPrice();
        int price_sale = product.getPrice_sale();
        int discount = product.getSale();

        if (discount > 0) {
            SpannableString originalPriceStr = new SpannableString(product.getFormattedPrice());
            originalPriceStr.setSpan(new StrikethroughSpan(), 0, originalPriceStr.length(), 0);
            originalPriceStr.setSpan(
                    new ForegroundColorSpan(context.getResources().getColor(android.R.color.darker_gray)),
                    0,
                    originalPriceStr.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            SpannableString finalPriceStr = new SpannableString(String.format("%,d đ", price_sale));
            finalPriceStr.setSpan(
                    new ForegroundColorSpan(context.getResources().getColor(R.color.heart_color)),
                    0,
                    finalPriceStr.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            holder.tvProductPrice.setText(TextUtils.concat(originalPriceStr, "  ", finalPriceStr));
            holder.tvProductDiscount.setVisibility(View.VISIBLE);
            holder.tvProductDiscount.setText(product.getSale() + "%");
        } else {
            holder.tvProductPrice.setText(product.getFormattedPrice());
            holder.tvProductDiscount.setVisibility(View.GONE);
        }

        int totalSold = 0;
        for (Variation v : product.getVariations()) {
            totalSold += v.getSold();
        }
        holder.tvProductSold.setText("Đã bán: " + totalSold + " sp");

        // --- Rating + Review ---
        ApiService api = ApiClient.getApiService();
        // Trong onBindViewHolder của ProductAdapter
        api.getAverageRating(product.get_id()).enqueue(new Callback<RatingResponse>() {
            @Override
            public void onResponse(Call<RatingResponse> call, Response<RatingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    float avg = (float) response.body().getAvgRating();
                    int count = response.body().getTotalReviews();

                    // Cập nhật hiển thị sao bằng mảng stars[]
                    updateStarDisplay(holder.stars, holder.txtAverageRating, avg, count);
                } else {
                    updateStarDisplay(holder.stars, holder.txtAverageRating, 0f, 0);
                }
            }

            @Override
            public void onFailure(Call<RatingResponse> call, Throwable t) {
                updateStarDisplay(holder.stars, holder.txtAverageRating, 0f, 0);
            }
        });

        // --- Load ảnh sản phẩm ---
        Glide.with(context)
                .load(ApiClient.IMAGE_URL + product.getAvt_imgproduct())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .override(300, 300)
                .centerCrop()
                .into(holder.ivProductImage);

        // --- Favorite ---
        updateFavoriteIcon(holder.imgFavorite, product);

        holder.ivProductImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChiTietSanPham.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
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

            ApiService api1 = ApiClient.getApiService();
            boolean wasFavorite = FavoriteFragment.isFavorite(p);

            if (wasFavorite) {
                FavoriteFragment.remove(context, p);
                updateFavoriteIcon(holder.imgFavorite, p);
                notifyItemChanged(pos);
                Toast.makeText(context, "Đã xoá khỏi yêu thích", Toast.LENGTH_SHORT).show();

                api1.removeFavorite(userId, p.get_id()).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            FavoriteFragment.add(context, p);
                            updateFavoriteIcon(holder.imgFavorite, p);
                            notifyItemChanged(pos);
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(context, "Xoá yêu thích thất bại", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        FavoriteFragment.add(context, p);
                        notifyItemChanged(pos);
                        Toast.makeText(context, "Lỗi kết nối khi xoá yêu thích", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                FavoriteFragment.add(context, p);
                notifyItemChanged(pos);

                Favorite fav = new Favorite(userId, product.get_id());
                api1.addFavorite(fav).enqueue(new Callback<Favorite>() {
                    @Override
                    public void onResponse(Call<Favorite> call, Response<Favorite> response) {
                        if (response.isSuccessful()) {
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

                            FavoriteFragment.remove(context, p);
                            notifyItemChanged(pos);
                        }
                    }

                    @Override
                    public void onFailure(Call<Favorite> call, Throwable t) {
                        FavoriteFragment.remove(context, p);
                        notifyItemChanged(pos);

                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "Lỗi kết nối khi thêm yêu thích", Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            }
        });

        // --- Giỏ hàng ---
        holder.ivCart.setOnClickListener(view -> {
            CartBottomSheetDialog dialog = new CartBottomSheetDialog(context, product);
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "CartBottomSheetDialog");
        });
    }
    {   // khối init

    }

    private void updateStarDisplay(ImageView[] stars, TextView txtAverageRating, float avgRating, int totalReviews) {
        for (int i = 0; i < stars.length; i++) {
            if (avgRating >= i + 1) {
                stars[i].setImageResource(R.drawable.star); // sao đầy
            } else if (avgRating > i && avgRating < i + 1) {
                stars[i].setImageResource(R.drawable.left_half_star); // sao nửa
            } else {
                stars[i].setImageResource(R.drawable.star_filled); // sao rỗng
            }
        }

        txtAverageRating.setText(
                String.format(Locale.getDefault(), "%.1f/5 (%d)", avgRating, totalReviews)
        );
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
        ImageView ivProductImage, imgFavorite, ivCart;
        TextView tvProductName;
        TextView tvProductPrice;
        TextView tvProductSold;
        TextView tvProductDiscount;
        TextView txtAverageRating;
        ImageView[] stars;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductSold = itemView.findViewById(R.id.tvProductSold);
            tvProductDiscount = itemView.findViewById(R.id.tvProductDiscount);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            ivCart = itemView.findViewById(R.id.ivCart);

            txtAverageRating = itemView.findViewById(R.id.txtAverageRating);

            stars = new ImageView[]{
                    itemView.findViewById(R.id.star1_),
                    itemView.findViewById(R.id.star2_),
                    itemView.findViewById(R.id.star3_),
                    itemView.findViewById(R.id.star4_),
                    itemView.findViewById(R.id.star5_)
            };
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
