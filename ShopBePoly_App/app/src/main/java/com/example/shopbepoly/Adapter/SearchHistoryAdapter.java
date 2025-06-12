package com.example.shopbepoly.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.R;

import java.util.List;
import java.util.ArrayList;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {
    private static final String TAG = "SearchHistoryAdapter";
    private List<String> searchHistory;
    private OnSearchHistoryClickListener listener;
    private OnDeleteItemClickListener deleteListener;

    public interface OnSearchHistoryClickListener {
        void onSearchHistoryClick(String query);
    }

    public interface OnDeleteItemClickListener {
        void onDeleteItemClick(String query);
    }

    public SearchHistoryAdapter(OnSearchHistoryClickListener listener) {
        this.searchHistory = new ArrayList<>();
        this.listener = listener;
    }

    public void setOnDeleteItemClickListener(OnDeleteItemClickListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String query = searchHistory.get(position);
        holder.tvSearchQuery.setText(query);
        
        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSearchHistoryClick(query);
            }
        });

        // Set click listener for delete button
        holder.btnDeleteItem.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteItemClick(query);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchHistory != null ? searchHistory.size() : 0;
    }

    public void updateData(List<String> newHistory) {
        Log.d(TAG, "Updating data with size: " + (newHistory != null ? newHistory.size() : 0));
        this.searchHistory = newHistory;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSearchQuery;
        ImageView btnDeleteItem;

        ViewHolder(View itemView) {
            super(itemView);
            tvSearchQuery = itemView.findViewById(R.id.tv_search_query);
            btnDeleteItem = itemView.findViewById(R.id.btn_delete_item);
        }
    }
} 