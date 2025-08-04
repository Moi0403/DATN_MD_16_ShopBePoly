package com.example.shopbepoly.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopbepoly.DTO.Banner;
import com.example.shopbepoly.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private List<Banner> bannerList;
    private String serverBaseUrl;
    public BannerAdapter(List<Banner> bannerList, String serverBaseUrl) {
        this.bannerList = bannerList;
        this.serverBaseUrl = serverBaseUrl;
    }
    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = bannerList.get(position);
        String imageUrl = banner.getImageUrl();


        if (imageUrl != null && imageUrl.startsWith("/")) {
            imageUrl = imageUrl.substring(1);
        }

        String fullUrl = serverBaseUrl + imageUrl;

        Glide.with(holder.itemView.getContext())
                .load(fullUrl)
                .placeholder(R.drawable.banner) // Placeholder khi ảnh đang tải
                .error(R.drawable.banner1)     // Ảnh hiển thị khi lỗi
                .into(holder.imgBanner);
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBanner;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}