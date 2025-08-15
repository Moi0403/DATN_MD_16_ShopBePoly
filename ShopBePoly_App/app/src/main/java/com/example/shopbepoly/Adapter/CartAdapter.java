package com.example.shopbepoly.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.Variation;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Screen.ChiTietSanPham;
import com.example.shopbepoly.fragment.CartBottomSheetDialog;
import com.example.shopbepoly.fragment.CartFragment;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Log.d("CartAdapter", "=== DEBUG BIND VIEW HOLDER ===");
        Log.d("CartAdapter", "Position: " + position);
        Log.d("CartAdapter", "Product Name: " + (product != null ? product.getNameproduct() : "NULL"));
        Log.d("CartAdapter", "cart.getImg_cart(): " + cart.getImg_cart());
        Log.d("CartAdapter", "Is img_cart null/empty: " + (cart.getImg_cart() == null || cart.getImg_cart().isEmpty()));
        Log.d("CartAdapter", "================================");
        if (cart != null && cart.getIdProduct() != null) {
            String imageUrl = cart.getImg_cart();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Log.d("CartAdapter", "Loading image: " + imageUrl);
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(holder.imvAVT);
            } else {
                Log.w("CartAdapter", "Image URL is null/empty, using fallback");
                // Fallback sang ảnh chính nếu img_cart rỗng
                String fallbackImg = product.getAvt_imgproduct();
                if (fallbackImg != null && !fallbackImg.trim().isEmpty()) {
                    Glide.with(context)
                            .load(fallbackImg)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(holder.imvAVT);
                } else {
                    // Set placeholder nếu không có ảnh nào
                    holder.imvAVT.setImageResource(R.drawable.ic_launcher_background);
                }
            }
            holder.tvName.setText(product.getNameproduct() != null ? product.getNameproduct() : "N/A");

            // ====== PHẦN HIỂN THỊ GIÁ ======
            int salePrice = product.getPrice_sale();
            int originalPrice = product.getPrice();

            if (salePrice > 0 && salePrice < originalPrice) {
                // Có giảm giá → hiển thị giá sale
                holder.tvPrice.setText(String.format("%,d đ", salePrice * cart.getQuantity()));
                holder.tvPrice.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                // Không giảm giá → lấy giá gốc
                holder.tvPrice.setText(String.format("%,d đ", originalPrice * cart.getQuantity()));
                holder.tvPrice.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            }

            holder.tvMau.setText("Màu: "+cart.getColor());
            holder.tvSize.setText("Size: "+cart.getSize());
            holder.tvQuantity.setText(cart.getQuantity() > 0 ? String.valueOf(cart.getQuantity()) : "1");
        } else {
            Log.e("CartAdapter", "Cart hoặc Product null ở vị trí " + position);
        }
        holder.imv_tang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQty = cart.getQuantity();
                int stockQty = 9999; // Mặc định nếu không tìm thấy variation

                // Tìm variation tương ứng với size + color để lấy tồn kho
                for (Variation v : product.getVariations()) {
                    if (v.getSize() == cart.getSize()) {
                        if (cart.getColor() == null || v.getColor() == null ||
                                cart.getColor().equalsIgnoreCase(v.getColor().getName())) {
                            stockQty = v.getStock();
                            break;
                        }
                    }
                }

                if (currentQty < stockQty) {
                    cart.setQuantity(currentQty + 1);

                    // 🔹 Lấy giá thực tế: nếu có sale thì dùng salePrice
                    int priceToUse = (product.getPrice_sale() > 0 && product.getPrice_sale() < product.getPrice())
                            ? product.getPrice_sale()
                            : product.getPrice();

                    cart.setTotal(priceToUse * cart.getQuantity());
                    holder.tvPrice.setText(String.format("%,d đ", cart.getTotal()));
                    holder.tvQuantity.setText(String.valueOf(cart.getQuantity()));

                    update_quantity(cart);
                    updateTotalPrice();
                } else {
                    Toast.makeText(context, "Vượt quá số lượng kho (" + stockQty + ")", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.imv_giam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int slgHT = cart.getQuantity();
                if (slgHT > 1) {
                    cart.setQuantity(slgHT - 1);

                    // 🔹 Lấy giá thực tế
                    int priceToUse = (product.getPrice_sale() > 0 && product.getPrice_sale() < product.getPrice())
                            ? product.getPrice_sale()
                            : product.getPrice();

                    cart.setTotal(priceToUse * cart.getQuantity());
                    holder.tvPrice.setText(String.format("%,d đ", cart.getTotal()));
                    holder.tvQuantity.setText(String.valueOf(cart.getQuantity()));

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
        holder.cbk_add.setChecked(cart.getStatus() == 1);
        holder.cbk_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = holder.cbk_add.isChecked();

                cart.setStatus(isChecked ? 1 : 0);
                cart.setChecked(isChecked);

                update_quantity(cart);
                updateTotalPrice();
            }
        });

        holder.imvAVT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChiTietSanPham.class);
                intent.putExtra("product",product);
                context.startActivity(intent);
            }
        });

        holder.item_mausize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CartBottomSheetDialog dialog = new CartBottomSheetDialog(
                        context,
                        product,
                        frag_total,  // Truyền thẳng fragment implement listener
                        cart.get_id()
                );

                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "CartBottomSheetDialog");
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
        TextView tvName, tvPrice, tvSize, tvQuantity, tvMau;
        LinearLayout item_mausize;
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
            tvMau = itemView.findViewById(R.id.tv_mau);
            item_mausize = itemView.findViewById(R.id.item_mausize);
        }
    }
    private void update_quantity(Cart cart){
        ApiService apiService = ApiClient.getApiService();
        Call<Cart> call = apiService.upCart(cart.get_id(), cart);
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    Log.e("GioHang_ADT", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {

            }
        });
    }

    private void showDE(int position){
        Cart cart = list_cart.get(position);
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.thongbao);
        builder.setTitle("Thông báo");
        builder.setMessage("Bạn có chắc chắn muốn xóa " + cart.getIdProduct().getNameproduct() + " không?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            ApiService apiService = ApiClient.getApiService();
            Call<ResponseBody> call = apiService.delCart(cart.get_id());

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                        list_cart.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list_cart.size());
                        updateTotalPrice();
                        if (frag_total != null) {
                            frag_total.LoadCart(userId); // <== cần hàm này
                        }
                        frag_total.checkbox_select_all.setChecked(false);
                    } else {
                        Toast.makeText(context, "Xóa thất bại (Mã lỗi: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(context, "Lỗi kết nối khi xóa", Toast.LENGTH_SHORT).show();
                    Log.e("CartAdapter", "Lỗi khi gọi API xóa", t);
                }
            });
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
    public void deleteSelectedCarts() {
        List<String> idsToDelete = new ArrayList<>();
        for (Cart cart : list_cart) {
            if (cart.getStatus() == 1) {
                idsToDelete.add(cart.get_id());
            }
        }

        if (idsToDelete.isEmpty()) {
            Toast.makeText(context, "Không có sản phẩm nào được chọn để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Map<String, List<String>> body = new HashMap<>();
        body.put("cartIds", idsToDelete);

        apiService.deleteCartItems(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Xóa các sản phẩm đã đặt thành công", Toast.LENGTH_SHORT).show();
                    list_cart.removeIf(cart -> cart.getStatus() == 1);
                    notifyDataSetChanged();
                    updateTotalPrice();
                    frag_total.checkbox_select_all.setChecked(false);
                } else {
                    Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối khi xóa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateTotalPrice() {
        int total = 0;
        for (Cart item : list_cart) {
            if (item.getStatus() == 1) {
                int priceToUse = (item.getIdProduct().getPrice_sale() > 0
                        && item.getIdProduct().getPrice_sale() < item.getIdProduct().getPrice())
                        ? item.getIdProduct().getPrice_sale()
                        : item.getIdProduct().getPrice();
                total += priceToUse * item.getQuantity();
            }
        }
        frag_total.tvTotal.setText(String.format("Thành tiền: %,d đ", total));
    }
    public void selectAll(boolean isChecked) {
        for (Cart cart : list_cart) {
            cart.setStatus(isChecked ? 1 : 0);
            cart.setChecked(isChecked); // ĐỒNG BỘ trạng thái checked
        }
        notifyDataSetChanged();
        updateAllQuantities();
        updateTotalPrice();
    }
    private void updateAllQuantities() {
        for (Cart cart : list_cart) {
            update_quantity(cart);
        }
    }

    public List<Cart> getSelectedItems() {
        List<Cart> selected = new ArrayList<>();
        for (Cart cart : list_cart) {
            if (cart.getStatus() == 1) {
                selected.add(cart);
            }
        }
        return selected;
    }
    public void addCartOnServer(Cart cart) {
        ApiService apiService = ApiClient.getApiService();
        Call<Cart> call = apiService.addCart(cart);
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Thêm vào giỏ hàng thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Thêm giỏ hàng thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cart>call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối khi thêm giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateCartOnServer(Cart cart) {
        ApiService apiService = ApiClient.getApiService();
        Call<Cart> call = apiService.upCart(cart.get_id(), cart);
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Cập nhật số lượng thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối khi cập nhật", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
